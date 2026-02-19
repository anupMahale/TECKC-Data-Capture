# Configuration
$portName = "COM4"
$baudRate = 9600


# Create Serial Port Object
$port = New-Object System.IO.Ports.SerialPort $portName, $baudRate, None, 7, One

try {
    # Open the port
    $port.Open()
    Write-Host "Machine is ON. Connected to $portName."
    Write-Host "Sending data... (Press Ctrl+C to stop)"

    # Loop to simulate continuous measurements
    $counter = 11.583

    while($true){
        if ($port.BytesToRead -gt 0){
            # Read a single byte
            $incomingByte = $port.ReadByte()
            
            # Check if the byte is STX (Hex 0x02 is Decimal 2)
            if ($incomingByte -eq 2){

                Write-Host "`n[Received STX Trigger!] -> " -NoNewline
                
                # Create a fake reading (e.g., increasing by 0.1mm)
                $reading = "{0:N4}" -f $counter

                $message = "+ "  # Mimics Heidenhain format

                # Send it down the wire
                $port.Write($message)
                Write-Host "Sent: $message" -NoNewline

                $message = "$reading"  # Mimics Heidenhain format

                # Send it down the wire
                $port.Write($message)
                Write-Host "Sent: $message" -NoNewline

                $message = " ?"  # Mimics Heidenhain format

                # Send it down the wire
                $port.Write($message)
                Write-Host "Sent: $message" -NoNewline

                $message = "q"  # Mimics Heidenhain format

                # Send it down the wire
                $port.Write($message)
                Write-Host "Sent: $message" -NoNewline

                $counter += 0.1234
            }else{
                Write-Host "`n[Received unknown byte: $incomingByte]"
            }
        }
        Start-Sleep -Milliseconds 50
    }
}
catch {
    Write-Error "Could not open port $portName. Is it already in use?"
}
finally {
    if ($port.IsOpen) { $port.Close() }
    Write-Host "Machine Disconnected."
}