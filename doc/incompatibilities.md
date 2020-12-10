# 4.0.5

1. Renamed build variables:

  |old name|new name|
  |---|---|
  |updated_ref|ref|
  |updated_branch|branch|
  |updated_tag|tag|
  |pull_request_ids|pull_request_number|

1. Removed build variables: _on_branches_, _commit_tags_, 

# 3.2

Version 3.2 uses token authentication instead of password authentication in build spec for security 
consideration. As a result of this, password authentication specified previously will be cleared:

1. Submodule authentication
  
  You will need to use custom http/ssh clone credential with permission to access submodule projects to retrieve source. Refer to [usage scenario](https://code.onedev.io/projects/onedev-manual/blob/master/pages/clone-submodules-via-ssh.md) for an example.
  
2. Project dependency authentication

  You will need to define a job secret containing an access token in project build setting page, and then use that secret in project dependency definition. The access token should have permission to download dependency project artifacts of course.