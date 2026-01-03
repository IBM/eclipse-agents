rm -rf ~/.eclipseagents/checkpoints

mkdir ~/.eclipseagents/checkpoints
mkdir ~/.eclipseagents/checkpoints/1234-1234-1234-1234

echo "[user]\n  name = Gemini CLI\n  email = gemini-cli@google.com\n[commit]\n  gpgsign = false\n" > ~/.eclipseagents/checkpoints/1234-1234-1234-1234/.gitconfig
echo "TODO" > ~/.eclipseagents/checkpoints/1234-1234-1234-1234/.gitignore


git -C ~/.eclipseagents/checkpoints/1234-1234-1234-1234 init --initial-branch main
git -C ~/.eclipseagents/checkpoints/1234-1234-1234-1234 commit -m 'Initial commit' --allow-empty

#path of the shadow repository 
export GIT_DIR=~/.eclipseagents/checkpoints/1234-1234-1234-1234/.git

#path of the user's actual project to checkpoint
export GIT_WORK_TREE=~/git/eclipse-agents



# Executes git add . against the user's project directory (the GIT_WORK_TREE). This stages all current changes (modifications, new files, deletions).
git add .

# It runs git status to see if there are any actual changes.
git status

# If there are changes, it executes git commit -m "Snapshot for <tool_name>" --no-verify. The --no-verify flag is important to bypass any pre-commit hooks the user might have configured.
git commit -m "Snapshot for <tool_name>" --no-verify

# The commit hash of this new snapshot is returned. If there were no changes, the hash of the current HEAD is returned instead.
git rev-parse HEAD
