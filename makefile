
test:
	./gradlew test
# Whilst this breaks the one line per recipe rule, it is providing linls to where the output goes
	@ls -ld ./babble/build/reports/tests/testReleaseUnitTest/index.html
	@ls -ld ./babble/build/reports/tests/testDebugUnitTest/index.html
	@ls -ld ./sample/build/reports/tests/testReleaseUnitTest/index.html
	@ls -ld ./sample/build/reports/tests/testDebugUnitTest/index.html

