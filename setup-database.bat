@echo off
REM Script to run PostgreSQL setup files on Windows
REM This script reads database credentials from .env file and runs the SQL scripts

echo === PostgreSQL Setup Script ===
echo.

REM Check if .env file exists
if not exist .env (
    echo Error: .env file not found!
    echo Please create a .env file with your database credentials.
    exit /b 1
)

REM Load environment variables from .env file
for /f "usebackq tokens=1,2 delims==" %%a in (".env") do (
    if not "%%a"=="" if not "%%a:~0,1%"=="#" set %%a=%%b
)

REM Check if required variables are set
if "%POSTGRESQL_ADDON_HOST%"=="" (
    echo Error: Missing POSTGRESQL_ADDON_HOST in .env file
    exit /b 1
)
if "%POSTGRESQL_ADDON_PORT%"=="" (
    echo Error: Missing POSTGRESQL_ADDON_PORT in .env file
    exit /b 1
)
if "%POSTGRESQL_ADDON_DB%"=="" (
    echo Error: Missing POSTGRESQL_ADDON_DB in .env file
    exit /b 1
)
if "%POSTGRESQL_ADDON_USER%"=="" (
    echo Error: Missing POSTGRESQL_ADDON_USER in .env file
    exit /b 1
)
if "%POSTGRESQL_ADDON_PASSWORD%"=="" (
    echo Error: Missing POSTGRESQL_ADDON_PASSWORD in .env file
    exit /b 1
)

REM Set password environment variable
set PGPASSWORD=%POSTGRESQL_ADDON_PASSWORD%

echo Connecting to database:
echo   Host: %POSTGRESQL_ADDON_HOST%
echo   Port: %POSTGRESQL_ADDON_PORT%
echo   Database: %POSTGRESQL_ADDON_DB%
echo   User: %POSTGRESQL_ADDON_USER%
echo.

REM Construct connection string
set DB_URL=postgresql://%POSTGRESQL_ADDON_USER%@%POSTGRESQL_ADDON_HOST%:%POSTGRESQL_ADDON_PORT%/%POSTGRESQL_ADDON_DB%

REM Check if psql is installed
where psql >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: psql is not installed!
    echo Please install PostgreSQL client from https://www.postgresql.org/download/windows/
    exit /b 1
)

REM Test database connection
echo Testing database connection...
psql "%DB_URL%" -c "SELECT version();" >nul 2>nul
if %errorlevel% equ 0 (
    echo Connection successful!
) else (
    echo Connection failed!
    echo Please check your database credentials in .env file.
    exit /b 1
)

REM Run schema creation script
if exist "database\01_create_tables.sql" (
    echo Running schema creation script...
    psql "%DB_URL%" -f database\01_create_tables.sql
    echo Schema created successfully!
) else (
    echo Warning: database\01_create_tables.sql not found
)

REM Ask if user wants to load sample data
echo.
set /p LOAD_SAMPLE="Do you want to load sample data? (y/n) "
if /i "%LOAD_SAMPLE%"=="y" (
    if exist "database\02_sample_data.sql" (
        echo Loading sample data...
        psql "%DB_URL%" -f database\02_sample_data.sql
        echo Sample data loaded successfully!
    ) else (
        echo Warning: database\02_sample_data.sql not found
    )
) else (
    echo Skipping sample data.
)

REM Verify tables were created
echo.
echo Verifying tables...
psql "%DB_URL%" -c "\dt"

echo.
echo === Setup Complete! ===
echo.
echo You can now run the Spring Boot application:
echo   mvn spring-boot:run
echo   or
echo   docker-compose up

REM Unset password
set PGPASSWORD=
