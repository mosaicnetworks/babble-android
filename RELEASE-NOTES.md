# Creating a Release

Assume all feature branches have been merged into the develop branch
(including an updated CHANGELOG).

1. Ensure babble-go version in the babble build.gradle file is a released
version of babble-go. If it isn't (i.e. it refrences a git commit) then check
if there is a suitable release. If there is, adjust the babble-go version and
create a commit.

2. Merge master into develop - there may commits on the master branch that are
not on the develop branch (these should't be code commits, however just in case
do this merge).

3. Run all tests, confirm they pass. If they pass, a release can be created.

4. Update the CHANGELOG - replace "UNRELEASED" with version and date e.g.
"v0.6.0 (December 20, 2019)".

5. Update version, versionName and versionCode in the babble build.gradle
file. NOTE: there are THREE parameters here, all of which must be incremented.

6. Update the version number in the Quickstart section of README.md.

7. Create a git commit - e.g. "Bumped version to version 0.2.6".

8. Merge develop into master (a fast forward should not be done).

9. Create a git tag.

10. Push the develop and master branches to github.

11. Upload to bintray ```gradlew bintrayUpload```.
