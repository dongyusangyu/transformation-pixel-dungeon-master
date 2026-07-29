param(
	[string]$JaPath = "core/src/main/assets/messages/actors/actors_ja.properties",
	[string]$ZhPath = "core/src/main/assets/messages/actors/actors_zh.properties",
	[string]$CachePath = "tools/actors_ja_translation_cache.json",
	[switch]$RestoreOfficialOnly
)

$ErrorActionPreference = "Stop"

function Read-Properties([string[]]$Lines) {
	$result = @{}
	foreach ($line in $Lines) {
		if ($line -match '^([^#!\s][^=]*)=(.*)$') {
			$result[$matches[1]] = $matches[2]
		}
	}
	return $result
}

function Protect-Text([string]$Text) {
	$protected = $Text.Replace('\n', '[[[NL]]]')
	$glossary = [ordered]@{
		'投掷武器' = '投擲武器'
		'生命值' = 'HP'
		'天赋' = '天賦'
		'回合' = 'ターン'
		'护盾' = 'シールド'
		'护甲' = '鎧'
		'英雄' = '勇士'
		'敌人' = '敵'
		'法杖' = '杖'
		'神器' = 'アーティファクト'
		'附魔' = 'エンチャント'
		'刻印' = '刻印'
		'充能' = 'チャージ'
		'等级' = 'レベル'
		'格' = 'タイル'
	}
	foreach ($term in $glossary.Keys) {
		$protected = $protected.Replace($term, $glossary[$term])
	}
	return $protected
}

function Restore-Text([string]$Text) {
	return $Text.Replace('[[[NL]]]', '\n').Trim()
}

function Needs-Retranslation([string]$Text) {
	if ($Text -match '[锛鐨浣犳槸涓€鍦ㄥ洖鍚堣繖]') {
		return $true
	}
	if ($Text -match '[这该们为与后时将获击层个类过还从发对处让给开关并则伤术无进战护备选择赋]' -or
		$Text -match '[가-힣]') {
		return $true
	}
	$plain = $Text -replace '%(?:\d+\$)?[0-9.]*[sdif]', '' -replace '[_\d\W]', ''
	return $plain.Length -ge 16 -and $plain -match '[\p{IsCJKUnifiedIdeographs}]' -and
		$plain -notmatch '[\p{IsHiragana}\p{IsKatakana}]'
}

function Translate-Batch([array]$Batch) {
	$parts = [System.Collections.Generic.List[string]]::new()
	for ($i = 0; $i -lt $Batch.Count; $i++) {
		$parts.Add("[[[ENTRY_$i]]]")
		$parts.Add((Protect-Text $Batch[$i].Value))
	}

	$query = [Uri]::EscapeDataString(($parts -join "`n"))
	$instances = @(
		'https://lingva.ml',
		'https://translate.plausibility.cloud',
		'https://lingva.ml',
		'https://translate.plausibility.cloud'
	)

	$response = $null
	for ($attempt = 1; $attempt -le 4; $attempt++) {
		try {
			$instance = $instances[($attempt - 1) % $instances.Count]
			$response = Invoke-RestMethod `
				-Uri "$instance/api/v1/zh/ja/$query" `
				-Method Get `
				-TimeoutSec 60
			break
		} catch {
			if ($attempt -lt 4) {
				Start-Sleep -Seconds (2 * $attempt)
			}
		}
	}

	if ($null -eq $response) {
		if ($Batch.Count -gt 1) {
			$middle = [Math]::Floor($Batch.Count / 2)
			$left = @(Translate-Batch @($Batch[0..($middle - 1)]))
			$right = @(Translate-Batch @($Batch[$middle..($Batch.Count - 1)]))
			$result = @{}
			foreach ($part in @($left, $right)) {
				foreach ($key in $part.Keys) {
					$result[$key] = $part[$key]
				}
			}
			return $result
		}
		Write-Warning "Skipped untranslatable entry: $($Batch[0].Key)"
		return @{}
	}

	$translated = $response.translation
	$matches = [regex]::Matches(
		$translated,
		'(?s)\[\[\[ENTRY_(\d+)\]\]\]\s*(.*?)(?=\[\[\[ENTRY_\d+\]\]\]|\z)'
	)
	if ($matches.Count -ne $Batch.Count) {
		throw "Translation batch split failed: expected $($Batch.Count), got $($matches.Count)"
	}

	$result = @{}
	foreach ($match in $matches) {
		$index = [int]$match.Groups[1].Value
		$result[$Batch[$index].Key] = Restore-Text $match.Groups[2].Value
	}
	return $result
}

$currentJaLines = Get-Content -LiteralPath $JaPath -Encoding UTF8
$currentZhLines = Get-Content -LiteralPath $ZhPath -Encoding UTF8
$headJaLines = git show "HEAD:$JaPath"
$headZhLines = git show "HEAD:$ZhPath"

$currentJa = Read-Properties $currentJaLines
$currentZh = Read-Properties $currentZhLines
$headJa = Read-Properties $headJaLines
$headZh = Read-Properties $headZhLines

$replacements = @{}
$translate = [System.Collections.Generic.List[object]]::new()
$translationCache = @{}
if (Test-Path -LiteralPath $CachePath) {
	$cached = Get-Content -LiteralPath $CachePath -Raw -Encoding UTF8 | ConvertFrom-Json
	foreach ($property in $cached.psobject.Properties) {
		$translationCache[$property.Name] = $property.Value
	}
}

foreach ($key in $currentJa.Keys) {
	if ($headJa.ContainsKey($key) -and
			$headZh.ContainsKey($key) -and
			$currentZh.ContainsKey($key) -and
			$headZh[$key] -ceq $currentZh[$key]) {
		$replacements[$key] = $headJa[$key]
	} elseif ($currentZh.ContainsKey($key) -and $currentZh[$key] -ne '') {
		if ($translationCache.ContainsKey($key)) {
			$replacements[$key] = $translationCache[$key]
		} elseif (!$RestoreOfficialOnly -and (Needs-Retranslation $currentJa[$key])) {
			$translate.Add([pscustomobject]@{
				Key = $key
				Value = $currentZh[$key]
			})
		}
	}
}

$orderedTranslate = @($translate | Sort-Object Key)
$batchSize = 8
for ($start = 0; $start -lt $orderedTranslate.Count; $start += $batchSize) {
	$end = [Math]::Min($start + $batchSize - 1, $orderedTranslate.Count - 1)
	$batch = @($orderedTranslate[$start..$end])
	$batchResult = Translate-Batch $batch
	foreach ($key in $batchResult.Keys) {
		$replacements[$key] = $batchResult[$key]
		$translationCache[$key] = $batchResult[$key]
	}
	$translationCache |
		ConvertTo-Json -Depth 3 |
		Set-Content -LiteralPath $CachePath -Encoding UTF8
	Write-Progress `
		-Activity 'Rebuilding Japanese actor messages' `
		-Status "$($end + 1) / $($orderedTranslate.Count)" `
		-PercentComplete ((($end + 1) * 100) / $orderedTranslate.Count)
	Start-Sleep -Milliseconds 750
}

$output = foreach ($line in $currentJaLines) {
	if ($line -match '^([^#!\s][^=]*)=(.*)$' -and $replacements.ContainsKey($matches[1])) {
		"$($matches[1])=$($replacements[$matches[1]])"
	} else {
		$line
	}
}

$utf8NoBom = [System.Text.UTF8Encoding]::new($false)
[System.IO.File]::WriteAllLines((Resolve-Path $JaPath), $output, $utf8NoBom)

Write-Output "Restored/retranslated $($replacements.Count) entries; translated $($orderedTranslate.Count) custom entries."
