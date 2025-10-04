# Conventions

## Service Methods

1. Various service methods will not check permissions, except for those with subject param
1. User param of service methods are used to indicate who performs the action, instead of requiring permission check
1. Service methods will not audit changes, unless stated explicitly
