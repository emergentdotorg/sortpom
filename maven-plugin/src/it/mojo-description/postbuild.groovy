description = new File(basedir, 'description.txt')
expected_description = new File(basedir, 'expected_description.txt')

assert description.exists()
def expected = expected_description.text
        .replaceAll('@pom.version@', projectversion)
assert description.text.replaceAll('\r','').contains(expected.replaceAll('\r',''))

return true
