$ErrorActionPreference = "Stop"

$Server = "103.236.98.149"
$Port = 44140
$User = "root"
$RemoteDir = "/opt/tpd-cloud"
$SnapshotDir = "$RemoteDir/cloud_snapshots"
$DbPath = "$RemoteDir/cloud_data/talent_cloud.sqlite3"

Write-Host "Deploying UUID cloud update to $User@$Server..."
Write-Host "You will be prompted for the SSH password by ssh/scp."

$preDeploy = @"
set -e
mkdir -p "$SnapshotDir"
systemctl stop talent-cloud-admin.service || true
systemctl stop talent-cloud.service || true
if [ -f "$DbPath" ]; then
  cp "$DbPath" "$SnapshotDir/pre_uuid_deploy_`$(date +%Y%m%d_%H%M%S).sqlite3"
fi
"@

ssh -p $Port "$User@$Server" $preDeploy

scp -P $Port "$PSScriptRoot/cloud_backend.py" "$User@$Server`:$RemoteDir/cloud_backend.py"
scp -P $Port "$PSScriptRoot/admin_console.py" "$User@$Server`:$RemoteDir/admin_console.py"
scp -P $Port "$PSScriptRoot/../core/src/main/assets/messages/actors/actors_zh.properties" "$User@$Server`:$RemoteDir/actors_zh.properties"

$postDeploy = @"
set -e
python3 -m py_compile "$RemoteDir/cloud_backend.py" "$RemoteDir/admin_console.py"
systemctl daemon-reload
systemctl enable --now talent-cloud.service talent-cloud-admin.service
systemctl restart talent-cloud.service talent-cloud-admin.service
sleep 2
systemctl is-active talent-cloud.service
systemctl is-active talent-cloud-admin.service
curl -fsS http://127.0.0.1:44140/api/aggregate >/tmp/tpd_aggregate_check.json
python3 - <<'PY'
import json
with open('/tmp/tpd_aggregate_check.json', 'r', encoding='utf-8') as f:
    data = json.load(f)
assert data.get('ok') is True
print('aggregate endpoint ok')
PY
"@

ssh -p $Port "$User@$Server" $postDeploy

Write-Host "UUID cloud update deployment finished."
