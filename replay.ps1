$services = @(
    @{
        RoutingKey = "replay.book"
        TargetService = "book-service"
    },
    @{
        RoutingKey = "replay.user"
        TargetService = "user-service"
    },
    @{
        RoutingKey = "replay.loan"
        TargetService = "loan-service"
    }
)

$credentials = New-Object pscredential(
    "libraryApp",
    (ConvertTo-SecureString "libraryAppTest!" -AsPlainText -Force)
)

foreach ($service in $services) {

    $message = @{
        targetService = $service.TargetService
        occurredAt = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    } | ConvertTo-Json -Compress

    $body = @{
        properties = @{}
        routing_key = $service.RoutingKey
        payload = $message
        payload_encoding = "string"
    } | ConvertTo-Json -Compress

    Invoke-RestMethod `
        -Uri "http://localhost:15672/api/exchanges/%2F/replay.exchange/publish" `
        -Method Post `
        -Credential $credentials `
        -ContentType "application/json" `
        -Body $body

    Write-Host "Replay published for $($service.TargetService)"
}

Write-Host ""
Write-Host "Replay messages published successfully."
