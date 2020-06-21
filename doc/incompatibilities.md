# 3.2

Version 3.2 uses token authentication instead of password authentication in build spec for security 
consideration. As a result of this, password authentication specified previously will be 
cleared, and you should configure token authentication instead. Below settings in job 
definition will be affected:

1. Submodule authentication
2. Project dependency authentication