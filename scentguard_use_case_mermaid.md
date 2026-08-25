# Use Case Diagram Code (Mermaid.js)

Copy and paste this code into the [Mermaid Live Editor](https://mermaid.live/) to generate the diagram.

```mermaid
graph LR
    %% Actors
    Manager((Restaurant Manager))
    Staff((Restaurant Staff))
    Hardware((ScentGuard Hardware))

    subgraph "ScentGuard System Boundary"
        UC1(Account Management)
        UC2(Create Restaurant Profile)
        UC3(Join via Invite Code)
        UC4(Monitor Air Quality)
        UC5(Manual Fan Override)
        UC6(Staff Management)
        UC7(Data Analytics/Reports)
        UC8(System Logs & Alerts)
    end

    %% Manager Interactions
    Manager --- UC1
    Manager --- UC2
    Manager --- UC4
    Manager --- UC5
    Manager --- UC6
    Manager --- UC7
    Manager --- UC8

    %% Staff Interactions
    Staff --- UC1
    Staff --- UC3
    Staff --- UC4
    Staff --- UC8

    %% Hardware Interactions
    Hardware --- UC4
    Hardware --- UC5
```
