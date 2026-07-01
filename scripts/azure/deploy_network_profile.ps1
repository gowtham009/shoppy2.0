param(
  [string]$RG = "gkshoppy-rg",
  [string]$LOCATION = "eastus",
  [string]$VNET = "gkshoppy-vnet",
  [string]$SUBNET = "gkshoppy-subnet",
  [string]$NETWORK_PROFILE = "gkshoppy-netprofile"
)

$template = Join-Path -Path (Get-Location) -ChildPath "scripts/azure/networkProfile.json"
if (-not (Test-Path $template)) {
  Write-Error "Template not found: $template. Run from repository root."
  exit 1
}

Write-Host "Using: RG=$RG, LOCATION=$LOCATION, VNET=$VNET, SUBNET=$SUBNET, NETWORK_PROFILE=$NETWORK_PROFILE"

az group create --name $RG --location $LOCATION | Out-Null

az deployment group create `
  --resource-group $RG `
  --template-file $template `
  --parameters networkProfileName=$NETWORK_PROFILE location=$LOCATION vnetName=$VNET subnetName=$SUBNET

Write-Host "Deployment finished."
