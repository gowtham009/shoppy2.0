param(
  [string]$RG = "gkshoppy-rg",
  [string]$LOCATION = "eastus",
  [string]$ACR = $("gkshoppyacr" + (Get-Date -UFormat %s)),
  [string]$PLAN = "gkshoppy-plan",
  [string]$WEBAPP = $("gkshoppy-webapp" + (Get-Date -UFormat %s))
)

Write-Host "Using values:`n  Resource group: $RG`n  Location: $LOCATION`n  ACR: $ACR`n  Plan: $PLAN`n  WebApp: $WEBAPP"

Write-Host "`nEnsure you're logged in: az login"
$confirm = Read-Host "Continue? (y/N)"
if ($confirm -ne 'y' -and $confirm -ne 'Y') { Write-Host 'Cancelled.'; exit 1 }

az group create --name $RG --location $LOCATION | Out-Null

az acr create --resource-group $RG --name $ACR --sku Basic --location $LOCATION | Out-Null
$acrLogin = az acr show --name $ACR --resource-group $RG --query loginServer -o tsv
Write-Host "ACR login server: $acrLogin"

az appservice plan create --name $PLAN --resource-group $RG --is-linux --sku P1V2 | Out-Null

az webapp create --resource-group $RG --plan $PLAN --name $WEBAPP --deployment-container-image-name mcr.microsoft.com/dotnet/aspnet:7.0 | Out-Null

$subId = az account show --query id -o tsv
$scope = "/subscriptions/$subId/resourceGroups/$RG/providers/Microsoft.ContainerRegistry/registries/$ACR"

Write-Host "Creating service principal with acrpush role..."
$spJson = az ad sp create-for-rbac --name "github-sp-$ACR" --role acrpush --scopes $scope --sdk-auth
Write-Host "Service principal JSON (copy to GitHub secret AZURE_CREDENTIALS):`n$spJson"

Set-Content -Path "azure_sp_$ACR.json" -Value $spJson

Write-Host "`nOptionally enable ACR admin user:`n az acr update -n $ACR -g $RG --admin-enabled true`n az acr credential show -n $ACR -g $RG"

Write-Host "`nAdd these secrets to GitHub: AZURE_CREDENTIALS, ACR_NAME, AZURE_WEBAPP_NAME, AZURE_RESOURCE_GROUP, VITE_SUPABASE_URL, VITE_SUPABASE_ANON_KEY"
