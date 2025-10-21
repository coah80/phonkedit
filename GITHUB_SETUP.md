# GitHub Repository Setup Guide

This guide will help you set up your GitHub repository for CursedPhonk Renderer Edition.

## Prerequisites

### 1. Install Git
Download and install Git from: https://git-scm.com/download/win

During installation:
- Select "Git from the command line and also from 3rd-party software"
- Use default settings for other options

After installation, restart your terminal/PowerShell.

### 2. Configure Git (First Time Only)
```powershell
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

## Repository Setup Steps

### Step 1: Initialize Git Repository
```powershell
cd g:\phonkedit
git init
```

### Step 2: Add All Files
```powershell
git add .
```

### Step 3: Create Initial Commit
```powershell
git commit -m "Initial commit - CursedPhonk Renderer Edition v2.0.0"
```

### Step 4: Create GitHub Repository
1. Go to https://github.com/new
2. Repository name: `cursedphonk-renderer`
3. Description: `YouTube Shorts Phonk Edit effect for Minecraft 1.21.1 using Renderer library`
4. Choose Public (recommended) or Private
5. **Do NOT** check "Add a README file" (we already have one)
6. **Do NOT** check "Add .gitignore" (we already have one)
7. **Do NOT** choose a license (we already have one)
8. Click "Create repository"

### Step 5: Connect to GitHub
After creating the repository, GitHub will show you commands. Run these:

```powershell
git remote add origin https://github.com/YOUR_USERNAME/cursedphonk-renderer.git
git branch -M main
git push -u origin main
```

Replace `YOUR_USERNAME` with your actual GitHub username.

## GitHub Actions Workflows

The following workflows are now set up:

### 1. **Build Workflow** (`.github/workflows/build.yml`)
- Runs on every push to `main` or `develop` branches
- Runs on all pull requests to `main`
- Builds the mod and uploads artifacts
- Can be manually triggered

### 2. **Release Workflow** (`.github/workflows/release.yml`)
- Runs when you create a version tag (e.g., `v2.0.0`)
- Automatically builds and creates a GitHub Release
- Attaches JAR files to the release

### 3. **PR Validation** (`.github/workflows/pr-check.yml`)
- Validates pull requests before merging
- Comments on PRs with build status

### 4. **Dependabot** (`.github/dependabot.yml`)
- Automatically checks for dependency updates weekly
- Creates PRs for outdated dependencies

## Creating a Release

To create a new release:

```powershell
# Make sure all changes are committed
git add .
git commit -m "Release v2.0.0"

# Create and push a tag
git tag v2.0.0
git push origin main
git push origin v2.0.0
```

The Release workflow will automatically:
- Build the mod
- Create a GitHub Release
- Upload the JAR files
- Generate release notes

## File Structure

### New Files Created:
- `.gitignore` - Prevents unnecessary files from being tracked
- `README.md` - Project documentation
- `LICENSE` - MIT License
- `CONTRIBUTING.md` - Contribution guidelines
- `gradle.properties.example` - Example Gradle configuration
- `.github/workflows/build.yml` - Build automation
- `.github/workflows/release.yml` - Release automation
- `.github/workflows/pr-check.yml` - PR validation
- `.github/dependabot.yml` - Dependency updates
- `.github/ISSUE_TEMPLATE/bug_report.md` - Bug report template
- `.github/ISSUE_TEMPLATE/feature_request.md` - Feature request template
- `.github/PULL_REQUEST_TEMPLATE.md` - PR template

### Files Excluded from Git (in .gitignore):
- `build/` - Build output
- `.gradle/` - Gradle cache
- `run/` - Minecraft client files
- `.idea/`, `.vscode/` - IDE settings
- `gradle.properties` - Local configuration (use `gradle.properties.example` as template)

## Repository Features

### Issue Tracker
- Automatically enabled on GitHub
- Custom templates for bug reports and feature requests
- Users will see template options when creating issues

### Pull Requests
- Custom PR template helps contributors provide necessary information
- Automated validation runs on all PRs

### Releases
- Automated release creation with tags
- JAR files automatically attached
- Release notes auto-generated

## Additional Tips

### Branching Strategy
Consider using:
- `main` - Stable releases only
- `develop` - Development branch
- `feature/name` - Feature branches

### Creating Feature Branches
```powershell
git checkout -b feature/new-effect
# Make your changes
git add .
git commit -m "Add new effect feature"
git push origin feature/new-effect
```

Then create a Pull Request on GitHub.

### Updating Your Local Repository
```powershell
git pull origin main
```

## Troubleshooting

### Authentication Issues
If pushing fails with authentication errors, you may need to:
1. Generate a Personal Access Token (PAT) on GitHub
2. Go to: Settings → Developer settings → Personal access tokens → Tokens (classic)
3. Generate new token with `repo` permissions
4. Use the token instead of your password when prompted

### Large Files
If you accidentally committed large files:
```powershell
git rm --cached path/to/large/file
git commit -m "Remove large file"
```

## Next Steps

1. Install Git if you haven't already
2. Follow the setup steps above
3. Push your code to GitHub
4. Enable GitHub Pages (optional) for documentation
5. Add topics/tags to your repository: `minecraft`, `fabric`, `mod`, `phonk`, `renderer`
6. Star your own repository!

## Need Help?

- GitHub Docs: https://docs.github.com
- Git Documentation: https://git-scm.com/doc
- GitHub Actions: https://docs.github.com/en/actions

Happy coding! 🎉
