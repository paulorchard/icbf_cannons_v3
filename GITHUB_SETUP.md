# GitHub Repository Setup

## Create Repository on GitHub

1. Go to https://github.com/new
2. Fill in the details:
   - **Repository name**: `icbf-cannons-v3`
   - **Description**: `ICBF Cannons v3.0 - A complete rewrite for Minecraft 1.20.1 Forge`
   - **Visibility**: Public (or Private if you prefer)
   - **Initialize**: Leave unchecked (we already have a local repo)
   
3. Click "Create repository"

## Link Local Repository to GitHub

After creating the repository, run these commands:

```bash
cd C:\Apps\icbf_cannons_v3

# Add GitHub remote
git remote add origin https://github.com/paulorchard/icbf-cannons-v3.git

# Push initial commit
git branch -M main
git push -u origin main
```

## Alternative: Using GitHub CLI

If you have GitHub CLI installed:

```bash
cd C:\Apps\icbf_cannons_v3
gh repo create icbf-cannons-v3 --public --source=. --remote=origin --push
```

## Repository Settings

After creation, consider:

1. **Branch Protection**: Enable for `main` branch
2. **Topics**: Add tags like `minecraft`, `forge`, `cannons`, `mod`
3. **About**: Set description and website
4. **Releases**: Will be used when v3.0 is ready

## Local Repository Info

- **Location**: `C:\Apps\icbf_cannons_v3`
- **Current branch**: `master` (will rename to `main`)
- **Commits**: 1 (initial structure)
- **Status**: Clean working tree

## Next Steps After GitHub Setup

1. Open the new project in VS Code
2. Update `mods.toml` version to 3.0.0
3. Update `build.gradle` version to 3.0.0
4. Create initial mod class
5. Set up creative tab
6. Make second commit: "Phase 1: Core infrastructure"
