$ErrorActionPreference = "Stop"

$Server = "103.236.98.149"
$User = "root"
$RemoteDir = "/opt/tpd-cloud"

Write-Host "This script deploys the talent cloud backend to $User@$Server."
Write-Host "You will be prompted for the SSH password by scp/ssh."

ssh "$User@$Server" "mkdir -p $RemoteDir"
scp "$PSScriptRoot/cloud_backend.py" "$User@$Server`:$RemoteDir/cloud_backend.py"
scp "$PSScriptRoot/talent-cloud.service" "$User@$Server`:/etc/systemd/system/talent-cloud.service"
ssh "$User@$Server" "systemctl daemon-reload && systemctl enable --now talent-cloud.service && systemctl status talent-cloud.service --no-pager"

Write-Host "Talent cloud backend deployment finished."
