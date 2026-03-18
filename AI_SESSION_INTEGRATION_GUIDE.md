# OneDev AI Agent Session Feature - Integration Points Analysis

## Executive Summary
This document provides concrete integration points for implementing an app-wide AI agent session feature similar to GitHub Copilot coding agent sessions. The OneDev codebase has robust patterns for real-time updates, permission checks, background tasks, and logging that can be directly reused.

---

## 1. GLOBAL SESSION LIST PAGE

### Recommended Integration Point: `/admin` hierarchy
**File Path**: `server-core/src/main/java/io/onedev/server/web/page/admin/aisetting/`

Create a new page:
- **Class**: `AiSessionListPage extends AdministrationPage`
- **Reference**: `/server-core/src/main/java/io/onedev/server/web/page/admin/buildsetting/agent/AgentListPage.java` (line 36+)

**Key Components to Reuse**:
1. **Pattern**: `SavedQueriesPanel<NamedAgentQuery>` (AgentListPage.java:61)
   - Provides saved query/filter functionality
   - Model: Create `NamedAiSessionQuery extends NamedQuery` 

2. **List Panel**: `DefaultDataTable` (LayoutPage.java:84)
   - Use for displaying sessions with columns: Name, Status, Created, Updated, User, Action
   - Reference: DefaultDataTable used in AdministrationPage descendants

3. **Filters**: 
   - Reference: `AgentFilterPanel.java` (9,235 lines showing filtering pattern)
   - Create `AiSessionFilterPanel` with criteria: Status (RUNNING, COMPLETED, FAILED, PAUSED), User, Created Date

4. **Permissions Check**:
   - Add to `SystemAdministration` permission or create new `ManageAiSessions` permission
   - Reference: `/server-core/src/main/java/io/onedev/server/security/permission/ManageJob.java`

**Model Class Required**:
```
server-core/src/main/java/io/onedev/server/model/AiSession.java
- Extend AbstractEntity
- Properties: id, name, status (enum), startedAt, completedAt, user (FK to User)
- transientFields: logContent, progressPercent, currentTask
- Method: getChangeObservables() returning Set<String> for each state
```

**Service Layer**:
```
server-core/src/main/java/io/onedev/server/service/AiSessionService.java
server-core/src/main/java/io/onedev/server/service/impl/DefaultAiSessionService.java
```

**Navigation Entry**:
- Add to `LayoutPage.java` (line 221+): 
  - SubMenu: "AI Settings" → "Session History" → `AiSessionListPage.class`
  - Location: After existing "AI Settings" admin section

---

## 2. TOPBAR INDICATOR FOR ACTIVE SESSIONS

### Integration Point: `LayoutPage.java` Topbar

**File Path**: `server-core/src/main/java/io/onedev/server/web/page/layout/LayoutPage.html` (line 37+)

### Implementation Strategy:

**A. Topbar Badge Component** (Right-align next to Chat icon)
```
HTML Location in LayoutPage.html (line 48-50 area):
<a wicket:id="activeSessions" 
   t:data-tippy-content="Active AI Sessions" 
   class="topbar-link">
   <wicket:svg href="ai" class="icon"/>
   <span wicket:id="sessionCount" class="badge badge-danger"></span>
</a>
```

**B. Java Implementation** (Add to LayoutPage.java ~line 400+):
```java
add(new AjaxLink<Void>("activeSessions") {
    @Override
    public void onClick(AjaxRequestTarget target) {
        setResponsePage(AiSessionListPage.class);
    }
    
    @Override
    protected void onConfigure() {
        setVisible(SecurityUtils.isAdministrator());
    }
});

add(new Label("sessionCount", new LoadableDetachableModel<Integer>() {
    @Override
    protected Integer load() {
        return aiSessionService.getActiveSessions().size();
    }
}));
```

**C. Live Update via WebSocket**:
- Add ChangeObserver to topbar component
- Observable: `"ai:session:*"` (wildcard for all session changes)
- Reference: LayoutPage.java uses ChangeObserver pattern for alerts (line 400+)

**D. CSS Badge**:
- Add to `layout.css` (line 10):
  ```css
  .topbar-link .badge {
    position: absolute;
    top: 2px;
    right: 2px;
    font-size: 10px;
  }
  ```

---

## 3. WEBSOCKET/CHANGE-OBSERVER LIVE UPDATES

### Infrastructure: Fully Reusable Pattern

**Core Files**:
- `/server-core/src/main/java/io/onedev/server/web/websocket/WebSocketService.java`
- `/server-core/src/main/java/io/onedev/server/web/behavior/ChangeObserver.java`

### Implementation:

**A. Create Event Broadcaster** 
```java
// File: server-core/src/main/java/io/onedev/server/web/websocket/AiSessionEventBroadcaster.java

@Singleton
public class AiSessionEventBroadcaster {
    private final WebSocketService webSocketService;
    
    @Inject
    public AiSessionEventBroadcaster(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }
    
    @Listen
    public void on(AiSessionStateChangedEvent event) {
        webSocketService.notifyObservablesChange(
            event.getSession().getChangeObservables(), 
            event.getSourcePage()
        );
    }
}
```

**Reference**: 
- Pattern: `BuildEventBroadcaster.java` (line 10-23)
- Annotation: `@Listen` from `io.onedev.server.event.Listen`
- Event system: All broadcasters in `/server-core/src/main/java/io/onedev/server/web/websocket/`

**B. Add to AiSession Model**:
```java
public Collection<String> getChangeObservables() {
    return Sets.newHashSet(
        String.format("ai:session:%d:detail", getId()),
        String.format("ai:session:%d:log", getId()),
        String.format("ai:session:%d:status", getId())
    );
}

public static String getDetailChangeObservable(Long sessionId) {
    return String.format("ai:session:%d:detail", sessionId);
}

public static String getLogChangeObservable(Long sessionId) {
    return String.format("ai:session:%d:log", sessionId);
}
```

**Reference**: `Build.java` implementation (line 1038+, getDetailChangeObservable pattern)

**C. Components Observing Sessions**:
- Any component displaying session data adds:
```java
add(new ChangeObserver() {
    @Override
    protected Collection<String> findObservables() {
        return Sets.newHashSet(AiSession.getDetailChangeObservable(sessionId));
    }
    
    @Override
    public void onObservableChanged(IPartialPageRequestHandler handler, 
                                   Collection<String> changedObservables) {
        handler.add(sessionDetailComponent);
    }
});
```

**Reference**: BuildLogPanel.java (line 47-88) for full implementation example

---

## 4. TERMINAL/XTERM INFRASTRUCTURE FOR SESSION LOGS

### Integration Point: Session Detail Page with Log View

**Files**:
- `/server-core/src/main/java/io/onedev/server/terminal/Terminal.java`
- `/server-core/src/main/java/io/onedev/server/terminal/TerminalService.java`
- `/server-core/src/main/java/io/onedev/server/web/asset/xterm/XtermResourceReference.java`

### Implementation:

**A. Create Session Log Component**:
```java
// File: server-core/src/main/java/io/onedev/server/web/component/ai/session/AiSessionLogPanel.java

public class AiSessionLogPanel extends GenericPanel<AiSession> {
    
    private int nextOffset;
    
    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        add(new ChangeObserver() {
            @Override
            public void onObservableChanged(IPartialPageRequestHandler handler, 
                                          Collection<String> changedObservables) {
                String newLog = aiSessionService.getLogSince(getModelObject().getId(), nextOffset);
                String script = String.format(
                    "onedev.server.aiSessionLog.appendLog('%s', %s);",
                    getMarkupId(), asJSON(newLog)
                );
                handler.appendJavaScript(script);
            }
            
            @Override
            protected Collection<String> findObservables() {
                return Sets.newHashSet(
                    AiSession.getLogChangeObservable(getModelObject().getId())
                );
            }
        });
    }
}
```

**Reference**:
- BuildLogPanel.java (line 31+) for exact pattern
- Log streaming: LogService usage pattern in BuildLogPanel (line 51)

**B. HTML Template** (`AiSessionLogPanel.html`):
```html
<div wicket:id="logContainer" class="ai-session-log" id="aiSessionLog">
    <!-- Rendered via JavaScript xterm appending -->
</div>
```

**C. JavaScript Integration**:
- Add: `server-core/src/main/java/io/onedev/server/web/component/ai/session/ai-session-log.js`
- Reference xterm library already in use: `/server-core/src/main/java/io/onedev/server/web/asset/xterm/`
- Use pattern from BuildLogPanel's `onedev.server.buildLog.appendLogEntries()`

**D. Service Method**:
```java
// In DefaultAiSessionService.java
public String getLogSince(Long sessionId, int offset) {
    AiSession session = get(sessionId);
    String fullLog = session.getLog() != null ? session.getLog() : "";
    return fullLog.substring(Math.min(offset, fullLog.length()));
}

public void appendLogEntry(Long sessionId, String entry) {
    AiSession session = get(sessionId);
    session.setLog((session.getLog() != null ? session.getLog() : "") + entry);
    update(session);
    
    // Broadcast update
    ListenerRegistry.getInstance().post(
        new AiSessionLogUpdatedEvent(session, null)
    );
}
```

---

## 5. PERMISSION CHECKS & ACCESS PATTERNS

### Two-Tier Permission Model:

**A. App-Wide Sessions** (Admin View)
```java
// File: server-core/src/main/java/io/onedev/server/security/permission/ManageAiSessions.java

public class ManageAiSessions implements BasePermission {
    @Override
    public boolean implies(Permission p) {
        return p instanceof ManageAiSessions;
    }
    
    @Override
    public boolean isApplicable(UserFacade user) {
        return true;  // Any user can have this permission
    }
}
```

**B. Project-Scoped Sessions** (Per-Project Visibility)
```java
// Extend AiSession with:
@ManyToOne(fetch=FetchType.LAZY)
@JoinColumn(nullable=true)  // Null = app-wide, Otherwise = project-scoped
private Project project;

// In SecurityUtils, add:
public static boolean canViewAiSession(AiSession session) {
    User user = getUser();
    if (user == null) return false;
    
    if (session.getProject() != null) {
        return canReadCode(session.getProject());  // Reuse existing project permission
    } else {
        return isAdministrator();  // App-wide requires admin
    }
}
```

**C. Permission Check in Page**:
```java
// In AiSessionListPage.java onInitialize():
if (!SecurityUtils.isAdministrator() && !SecurityUtils.hasPermission(new ManageAiSessions())) {
    throw new UnauthorizedException("No permission to view AI sessions");
}
```

**References**:
- `BasePermission.java` interface (line 8-12)
- `SecurityUtils.java` (line 96): `checkPermission()` pattern
- Project-scoped example: `AccessProject.java` (line 7-18)
- Usage: AgentListPage.java inherits from AdministrationPage which auto-checks admin

---

## 6. BACKGROUND TASK TRACKING & ACTIVITY FEED PATTERNS

### A. Task Button Pattern (For Manual Session Operations)
```java
// File: server-core/src/main/java/io/onedev/server/web/component/ai/session/StopSessionButton.java

public class StopSessionButton extends TaskButton {
    
    @Inject
    private AiSessionService sessionService;
    
    private Long sessionId;
    
    public StopSessionButton(String id, Long sessionId) {
        super(id);
        this.sessionId = sessionId;
    }
    
    @Override
    protected String getTitle() {
        return "Stop Session";
    }
    
    @Override
    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
        sessionService.stop(sessionId);
        // TaskButton automatically shows progress modal
    }
}
```

**Reference**:
- TaskButton pattern: `/server-core/src/main/java/io/onedev/server/web/component/taskbutton/TaskButton.java` (line 38+)
- Service pattern: `TaskFutureService.java` for async task tracking

**B. Activity Feed Pattern** (Session State Changes)
```java
// Create: server-core/src/main/java/io/onedev/server/model/support/AiSessionActivity.java

public class AiSessionActivity {
    private Date date;
    private User user;
    private String action;  // "STARTED", "PAUSED", "RESUMED", "COMPLETED", "FAILED"
    private String details; // Task description, error message, etc.
}

// In AiSession:
@ElementCollection(fetch=FetchType.LAZY)
@CollectionTable(name="O_AI_SESSION_ACTIVITY")
@OrderBy("date DESC")
private List<AiSessionActivity> activities = new ArrayList<>();
```

**Reference**:
- Pattern: `LastActivity.java` in model/support/
- Usage: Issue/PullRequest activity patterns in `/component/issue/activities/`

**C. Notification Pattern** (Optional - for notifications dashboard)
```java
// Use existing NotificationManager pattern
// Reference: server-core/src/main/java/io/onedev/server/notification/

public interface AiSessionNotificationManager {
    void notifySessionCompleted(AiSession session);
    void notifySessionFailed(AiSession session, String errorMessage);
}
```

---

## 7. PAGE STRUCTURE & RECOMMENDED PATHS

### Directory Organization:

```
server-core/src/main/java/io/onedev/server/
├── model/
│   ├── AiSession.java                         [NEW]
│   └── support/AiSessionActivity.java         [NEW]
├── service/
│   ├── AiSessionService.java                  [NEW]
│   └── impl/DefaultAiSessionService.java      [NEW]
├── web/
│   ├── websocket/
│   │   └── AiSessionEventBroadcaster.java     [NEW]
│   ├── component/ai/session/
│   │   ├── AiSessionLogPanel.java             [NEW]
│   │   ├── AiSessionLogPanel.html             [NEW]
│   │   ├── ai-session-log.js                  [NEW]
│   │   └── StopSessionButton.java             [NEW]
│   └── page/admin/aisetting/
│       ├── AiSessionListPage.java             [NEW]
│       ├── AiSessionListPage.html             [NEW]
│       ├── AiSessionDetailPage.java           [NEW]
│       ├── AiSessionDetailPage.html           [NEW]
│       ├── AiSessionFilterPanel.java          [NEW]
│       └── ai-session.css                     [NEW]
└── security/permission/
    └── ManageAiSessions.java                  [NEW]
```

---

## 8. IMPLEMENTATION PATH (PRIORITY ORDER)

### Phase 1: Core Data & Service (Day 1)
1. Create `AiSession.java` model with `getChangeObservables()`
2. Create `AiSessionService.java` & `DefaultAiSessionService.java`
3. Add database migration for `o_ai_session` table
4. Create `ManageAiSessions` permission class

### Phase 2: WebSocket & Events (Day 2)
1. Create `AiSessionEventBroadcaster.java`
2. Create `AiSessionStateChangedEvent.java` in event package
3. Wire broadcaster into CoreModule or via annotation scanning

### Phase 3: List Page & Navigation (Day 3)
1. Create `AiSessionListPage.java`, `.html`, filters
2. Add menu entry to `LayoutPage.java` (line 250+ area)
3. Create `AiSessionFilterPanel.java`
4. Add CSS to `ai-session.css`

### Phase 4: Detail Page & Logging (Day 4)
1. Create `AiSessionDetailPage.java` & `.html`
2. Create `AiSessionLogPanel.java` with xterm integration
3. Create JavaScript `ai-session-log.js`
4. Implement log streaming in service

### Phase 5: Topbar Indicator (Day 5)
1. Add topbar component to `LayoutPage.java`
2. Add HTML markup to `LayoutPage.html` (line 48)
3. Add ChangeObserver for live count updates
4. Style badge in `layout.css`

### Phase 6: Advanced Features (Optional)
1. Activity feed display
2. Notification system integration
3. Stop/Pause session buttons (TaskButton pattern)
4. Session search/filtering enhancements

---

## 9. KEY CODE EXAMPLES

### Add Observable to Model
```java
// In AiSession.java
public static final String PROP_STATUS = "status";
public static final String PROP_STARTED_AT = "startedAt";

public Collection<String> getChangeObservables() {
    return Sets.newHashSet(
        String.format("ai:session:%d", getId())
    );
}
```

### Broadcast Change
```java
// In service when updating session
AiSession session = update(sessionEntity);
ListenerRegistry.getInstance().post(
    new AiSessionUpdatedEvent(session, sourcePageKey)
);
```

### Component Observing Change
```java
add(new ChangeObserver() {
    @Override
    protected Collection<String> findObservables() {
        return Sets.newHashSet(
            AiSession.getDetailChangeObservable(sessionId)
        );
    }
    
    @Override
    public void onObservableChanged(IPartialPageRequestHandler handler, 
                                   Collection<String> changedObservables) {
        handler.add(detailComponent);
        handler.add(statusLabel);
    }
});
```

---

## 10. REFERENCES TO EXISTING PATTERNS

| Feature | Reference File | Location |
|---------|----------------|----------|
| **List Page** | `AgentListPage.java` | `/admin/buildsetting/agent/` |
| **Detail Page** | `BuildDetailPage.java` | `/project/builds/detail/` |
| **Log Panel** | `BuildLogPanel.java` | `/component/build/log/` |
| **Topbar Component** | `LayoutPage.java` | `/layout/` (line 400+) |
| **Change Observer** | `ChangeObserver.java` | `/behavior/` |
| **WebSocket Broadcaster** | `BuildEventBroadcaster.java` | `/websocket/` |
| **Permission** | `ManageJob.java` | `/security/permission/` |
| **Task Button** | `TaskButton.java` | `/component/taskbutton/` |
| **Navigation** | `LayoutPage.java` | `/layout/` (line 221+) |

---

## Conclusion

OneDev's architecture is highly conducive to implementing the AI agent session feature. The WebSocket/ChangeObserver infrastructure is production-tested, the permission system is flexible, and existing patterns for similar features (Build logs, Agent management) provide clear templates to follow. The recommended 5-6 day phased approach ensures progressive feature delivery with working functionality at each stage.

