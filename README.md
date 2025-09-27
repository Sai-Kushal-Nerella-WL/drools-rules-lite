# drools-rules-lite

End-to-end rules editor: Spring Boot backend + Angular 17 frontend + Excel rules management (Drools decision tables).

## Architecture

- **Backend**: Spring Boot 3 (Java 17, Gradle) - REST API for Excel rule management
- **Frontend**: Angular 17 + Angular Material - Interactive rules editor UI
- **Data**: Excel-based decision tables with dynamic parsing

## Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- Gradle 7.6+

### Backend (Port 8080)
```bash
cd apps/backend
./gradlew build test
./gradlew bootRun
```

### Frontend (Port 4200)
```bash
cd apps/frontend
npm install
npm start
```

### Docker Compose
```bash
docker-compose up --build
```

## URLs

- **Swagger API**: http://localhost:8080/swagger-ui.html
- **Rules Editor UI**: http://localhost:4200
- **Health Check**: http://localhost:8080/actuator/health

## Features

### Backend API
- `GET /rules` - Load decision table from Excel
- `POST /rules/validate` - Validate rule data
- `POST /rules/save` - Save rules back to Excel
- Dynamic Excel parsing with metadata preservation
- Comprehensive validation with detailed error reporting

### Frontend UI
- Interactive rules table with type-specific inputs
- Template panel showing parameter usage
- Real-time validation with error highlighting
- Add/clone/delete rows functionality
- Unsaved changes tracking

## Excel Format

The system reads `rules/DiscountRules.xlsx` with the following structure:
- Metadata rows (preserved during save)
- `RuleTable` marker row
- Headers: NAME, CONDITION, ACTION columns
- Template row with `$param` placeholders
- Data rows with rule values

## Development

### Backend Structure
```
apps/backend/src/main/java/com/worldlink/ruleseditor/
├── model/          # Data models (ColumnType, DecisionTable, etc.)
├── service/        # Business logic (ExcelService, ValidateService)
├── controller/     # REST endpoints (RulesController)
└── config/         # Configuration (CORS, etc.)
```

### Frontend Structure
```
apps/frontend/src/app/
├── models/         # TypeScript interfaces
├── services/       # HTTP services
└── components/     # Angular components
```

## Testing

- Backend: `./gradlew test` (includes round-trip Excel tests)
- Frontend: `npm test`
- E2E: Manual testing via browser interface
