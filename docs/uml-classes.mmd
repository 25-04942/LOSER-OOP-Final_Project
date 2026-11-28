classDiagram
    direction TB

    class AppSession {
        -Settings settings
        -Pet pet
        -UsageTracker tracker
        -WeeklyReport weekly
        +sync(UsageSource) void
        +rollOverIfNewDay() boolean
    }
    class Settings {
        -int dailyLimit
        -LocalTime resetTime
        +setDailyLimit(int) void
        +setThresholds(int, int, int) void
    }
    class UsageTracker {
        +addUsage(UsageRecord) void
        +getRatio() double
        +createDailyRecord() DailyRecord
    }
    class WeeklyReport {
        +addDay(DailyRecord) void
        +getWeeklyAverage() double
        +getTrend() Trend
    }
    class Pet {
        -String name
        -PetState currentState
        +updateState(double) boolean
    }
    class UsageRecord {
        -String appName
        -Category category
        -int minutesUsed
    }
    class DailyRecord {
        -LocalDate date
        -int totalMinutes
    }
    class Category {
        <<enumeration>>
        SOCIAL
        GAMES
        STUDY
        COMMUNICATION
        OTHER
    }
    class PetState {
        <<abstract>>
        +matches(double) boolean
        +getAppearance()* String
        +getMessage()* String
    }
    class HealthyState {
    }
    class TiredState {
    }
    class SickState {
    }
    class DevastatedState {
    }
    class Notifiable {
        <<interface>>
        +sendNotification(String) void
    }
    class NotificationService {
        +sendNotification(String) void
        #deliver(String) void
    }
    class UsageMonitor {
        +check(...) void
    }
    class UsageSource {
        <<interface>>
        +readUsageFor(LocalDate) List
    }
    class SampleUsageSource {
        +readUsageFor(LocalDate) List
    }
    class UsageReader {
        <<Kotlin>>
        +readUsageFor(LocalDate) List
    }
    class AndroidNotificationService {
        #deliver(String) void
    }

    HealthyState --|> PetState : extends
    TiredState --|> PetState : extends
    SickState --|> PetState : extends
    DevastatedState --|> PetState : extends
    AndroidNotificationService --|> NotificationService : extends
    NotificationService ..|> Notifiable : implements
    SampleUsageSource ..|> UsageSource : implements
    UsageReader ..|> UsageSource : implements
    AppSession *-- Settings : owns
    AppSession *-- Pet : owns
    AppSession *-- UsageTracker : owns
    AppSession *-- WeeklyReport : owns
    AppSession *-- UsageMonitor : owns
    Pet o-- PetState : has
    UsageTracker o-- UsageRecord : has
    WeeklyReport o-- DailyRecord : last 7 days
    UsageRecord --> Category : uses
    UsageTracker ..> DailyRecord : creates
    AppSession ..> UsageSource : reads from
    UsageMonitor ..> NotificationService : sends alerts

    style AppSession fill:#EFF6FF,stroke:#60A5FA,stroke-width:2px
    style Settings fill:#F0FDFA,stroke:#2DD4BF,stroke-width:2px
    style UsageTracker fill:#F0FDFA,stroke:#2DD4BF,stroke-width:2px
    style WeeklyReport fill:#F0FDFA,stroke:#2DD4BF,stroke-width:2px
    style Pet fill:#F0FDFA,stroke:#2DD4BF,stroke-width:2px
    style UsageRecord fill:#F0FDF4,stroke:#4ADE80,stroke-width:2px
    style DailyRecord fill:#F0FDF4,stroke:#4ADE80,stroke-width:2px
    style Category fill:#F0FDF4,stroke:#4ADE80,stroke-width:2px
    style PetState fill:#FFF7ED,stroke:#FB923C,stroke-width:2px
    style HealthyState fill:#FFF7ED,stroke:#FB923C,stroke-width:2px
    style TiredState fill:#FFF7ED,stroke:#FB923C,stroke-width:2px
    style SickState fill:#FFF7ED,stroke:#FB923C,stroke-width:2px
    style DevastatedState fill:#FFF7ED,stroke:#FB923C,stroke-width:2px
    style Notifiable fill:#F3F0FF,stroke:#A78BFA,stroke-width:2px
    style NotificationService fill:#F3F0FF,stroke:#A78BFA,stroke-width:2px
    style UsageMonitor fill:#F3F0FF,stroke:#A78BFA,stroke-width:2px
    style UsageSource fill:#FEFCE8,stroke:#FACC15,stroke-width:2px
    style SampleUsageSource fill:#FEFCE8,stroke:#FACC15,stroke-width:2px
    style UsageReader fill:#FDF4FF,stroke:#E879F9,stroke-width:2px
    style AndroidNotificationService fill:#FDF4FF,stroke:#E879F9,stroke-width:2px
