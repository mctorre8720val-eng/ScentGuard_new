# Activity Diagram Code (Mermaid.js)

Copy and paste this code into the [Mermaid Live Editor](https://mermaid.live/) to generate the diagram.

```mermaid
graph TD
    Start([Start System]) --> Init[Initialize WiFi & Sensors]
    
    subgraph Hardware Loop
        Init --> Read[Read MQ135 & DHT11]
        Read --> Push[Push Telemetry to Firestore]
        Push --> Watch[Listen for FanMode Change]
    end

    subgraph Cloud Layer
        Push --> UpdateDoc{Update Restaurant Doc}
        UpdateDoc --> Logic{Check Fan Mode}
    end

    subgraph App Layer
        UpdateDoc -.-> Sync[Snapshot Listener]
        Sync --> UI[Update Dashboard & Aura]
        
        UI --> Command[Manager sets Manual Mode]
        Command --> Write[Write New FanMode to DB]
    end

    Logic -- "MANUAL (ON/OFF)" --> WriteRelay[Physical Relay Control]
    Logic -- "AUTO" --> Threshold{PPM > Threshold?}
    
    Threshold -- "YES" --> Alert[Generate Notification]
    Alert --> WriteRelay
    Threshold -- "NO" --> Standby[Keep Fan Standby]
    
    WriteRelay --> Status[Update Heartbeat]
    Status --> Read
    Standby --> Read
    Write --> Watch
```
