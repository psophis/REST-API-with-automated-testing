name: CI Checks
on: [pull_request, push]
jobs:
    checks:
        runs-on: ubuntu-latest

    steps:
        - uses: actions/checkout@v6

        - name: Set up Java
          uses: actions/setup-java@v4
          with:
            distribution: temurin
            java-version: 21

        - name: Set up Gradle
          uses: gradle/gradle-build-action@v4

        - name: Build with Gradle
          run: ./gradlew build

        - name: Run Unit Tests
          run: ./gradlew test --tests "*ServiceTest"
          run: ./gradlew test --tests "*ControllerTest"
          run: ./gradlew test --tests "*RepositoryImplTest"

        - name: Run Integration Tests
          run: ./gradlew integrationTest --tests "*IntegrationTest"

        - name: Run E2E Tests
          run: ./gradlew apiTest --tests "*E2eTest"

        - name: Run Code Coverage
          run: ./gradlew jacocoTestReport

        - name: Run Linter
            run: ./gradlew ktlintCheck