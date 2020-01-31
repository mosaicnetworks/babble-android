# Creating a Release

Assume all feature branches have been merged into the develop branch
(including an updated CHANGELOG).

1. Pull develop and master branches from github.

2. Ensure babble-go version in the babble build.gradle file is a released
version of babble-go. If it isn't (i.e. it refrences a git commit) then check
if there is a suitable release. If there is, adjust the babble-go version and
create a commit.

3. Merge master into develop - there may be commits on the master branch that
are not on the develop branch (these should't be code commits, however just in
case do this merge).

4. Run all tests, confirm they pass. If they pass, a release can be created.

5. Update the CHANGELOG - replace "UNRELEASED" with version and date e.g.
"v0.6.0 (December 20, 2019)".

6. Update version, versionName and versionCode in the babble build.gradle
file. NOTE: there are THREE parameters here, all of which must be incremented.

7. Update the version number in the Quickstart section of README.md.

8. Create a git commit - e.g. "Bumped version to 0.2.6".

9. Merge develop into master (a fast forward should not be done).

10. Create a git tag.

11. Push the develop and master branches to github.

12. Upload to bintray ```gradlew bintrayUpload```.
