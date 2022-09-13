dependencies {
    api("com.squareup.retrofit2:retrofit:2.9.0")
    api("com.squareup.retrofit2:converter-gson:2.9.0")
    api("com.google.guava:guava:31.1-jre") {
        exclude("com.google.code.findbugs")
        exclude("com.google.errorprone")
        exclude("com.google.j2objc")
        exclude("org.checkerframework")
    }
    testImplementation("junit:junit:4.13.2")
}
