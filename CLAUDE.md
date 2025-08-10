# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

OneDev uses Maven as its build system with a multi-module structure.

### Essential Commands
- **Build the project**: `mvn clean compile`
- **Run tests**: `mvn test`
- **Package the application**: `mvn clean package`
- **Build without tests**: `mvn clean package -DskipTests`
- **Build specific module**: `mvn clean package -pl server-core`
- **Install to local repository**: `mvn clean install`

### Profiles
- **Community Edition**: `mvn clean package -Pce` (excludes enterprise features)
- **Default/Enterprise**: `mvn clean package` (includes all features)

### Testing
- **Run all tests**: `mvn test`
- **Run specific test class**: `mvn test -Dtest=ClassName`
- **Run tests for specific module**: `mvn test -pl server-core`

## Architecture Overview

OneDev is a comprehensive DevOps platform built with a sophisticated multi-module Maven architecture:

### Core Technology Stack
- **Web Framework**: Apache Wicket 7.18.0 (component-based UI)
- **REST API**: Jersey 2.38 (JAX-RS implementation)
- **Database/ORM**: Hibernate 5.4.24.Final with HikariCP connection pooling
- **Web Server**: Embedded Jetty 9.4.57
- **Dependency Injection**: Google Guice with custom plugin loading
- **Security**: Apache Shiro for authentication/authorization
- **Search**: Apache Lucene 8.7.0
- **Git**: JGit 5.13.3 for Git operations
- **Clustering**: Hazelcast 5.3.5 for distributed coordination

### Module Structure
- **server-core**: Core application logic, entities, and services
- **server-ee**: Enterprise edition features
- **server-plugin**: Plugin framework and all plugin implementations
- **server-product**: Final packaging and deployment artifacts

### Key Subsystems

#### 1. Application Bootstrap
- Main entry point: `server-core/src/main/java/io/onedev/server/OneDev.java`
- Module configuration: `server-core/src/main/java/io/onedev/server/CoreModule.java`
- Handles server lifecycle, clustering, and graceful shutdown

#### 2. Entity Management
Key domain entities and managers in `server-core/src/main/java/io/onedev/server/model/`:
- Project, User, Group, Role management
- Issue tracking with customizable workflows
- Pull request lifecycle and code review
- Build and CI/CD pipeline management
- Package registry operations

#### 3. Git Integration
- Full Git repository management via JGit
- Git hooks for policy enforcement in `server-core/src/main/java/io/onedev/server/git/`
- Code browsing, diff visualization, and blame tracking
- SSH server for Git operations

#### 4. Web Layer (Wicket)
- Component-based UI in `server-core/src/main/java/io/onedev/server/web/`
- AJAX-heavy interface with WebSocket support
- Project browsing, issue boards, pull request review interface

#### 5. REST API (Jersey)
- RESTful services in `server-core/src/main/java/io/onedev/server/rest/`
- Project, User, Build, Issue resources
- WebHook endpoints and package registry APIs

#### 6. CI/CD System
- YAML-based build specifications
- Multi-executor support (Kubernetes, Docker, Shell)
- Real-time log streaming and artifact management

#### 7. Plugin Architecture
- Extensible plugin system in `server-plugin/`
- Categories: build specs, executors, authenticators, importers, notifications, package registries, report processors
- Plugin contributions via Guice modules

## Development Patterns

### Code Organization
- **Package-by-feature**: Organized around business capabilities
- **Dependency Injection**: Guice-based DI throughout the application
- **Interface-based design**: For testability and modularity
- **Custom annotations**: Extensive use for validation and metadata

### Design Patterns Used
- Repository Pattern for data access
- Observer Pattern for event handling
- Command Pattern for Git operations
- Strategy Pattern for pluggable components
- Template Method for build processing

### Testing Strategy
- Unit tests in `src/test/java` directories
- Git operation tests with test repositories
- Component and integration tests
- Utility method tests
- Focus on testing business logic and Git operations

## Common Development Tasks

### Working with Entities
- Entities are in `server-core/src/main/java/io/onedev/server/model/`
- Use corresponding managers for database operations
- Follow JPA/Hibernate patterns for persistence

### Adding REST Endpoints
- Create resources in `server-core/src/main/java/io/onedev/server/rest/resource/`
- Follow Jersey/JAX-RS patterns
- Use existing security annotations for authentication

### Creating Plugins
- Extend `AbstractPlugin` class
- Implement appropriate interfaces for the plugin category
- Add Guice module configuration
- Place in appropriate `server-plugin/server-plugin-*` module

### Working with Git
- Use JGit APIs through OneDev's Git service layer
- Follow patterns in `server-core/src/main/java/io/onedev/server/git/`
- Handle Git operations asynchronously when possible

### Adding Web Components
- Create Wicket components in `server-core/src/main/java/io/onedev/server/web/`
- Follow existing component patterns and CSS frameworks
- Use AJAX for dynamic behavior

## Configuration and Deployment

### Key Configuration Files
- `server-product/system/conf/server.properties`: HTTP/SSH ports, clustering
- `server-product/system/conf/hibernate.properties`: Database configuration
- `server-product/system/conf/logback.xml`: Logging configuration

### Deployment Options
- Standalone JAR with embedded Jetty
- Docker containers (see `server-product/docker/`)
- Kubernetes via Helm charts (see `server-product/helm/`)

### Database Support
- PostgreSQL (recommended for production)
- MySQL/MariaDB
- HSQLDB (development/testing)

## Performance Considerations

### Caching
- Hibernate second-level cache with Hazelcast
- Build artifact caching
- Git object caching
- Web resource bundling

### Clustering
- Hazelcast-based clustering for high availability
- Distributed session management
- Leader election for coordinated operations

## Important Notes

- OneDev uses a custom plugin loading framework
- Git operations are central to the application architecture
- The system supports both community (CE) and enterprise (EE) editions
- Extensive use of Guice for dependency injection and plugin management
- Focus on performance and resource efficiency
- Battle-tested in production environments for 5+ years