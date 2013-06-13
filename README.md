= Start
- Clone Git repository
- `mvn package`
- create config.yaml based on target/config-example.yaml
- run `java -jar target/ldapgroups-*.jar server config.yaml`
- Go to http://localhost:8080/ for UI

= Description
LdapGroups is a webservice which allows to update members of LDAP groups. Service stores groups and their members
in internal database and then, in a background thread, tries to synchronize those db groups with actual LDAP groups.

Service consists of two parts - web service (src/) based on Dropwizard and UI frontend (ldapgroups.js/) based on
AngularJS.

The need for such service came from application which wanted to control access to network shares, network shares
based on Active Directory. The application had to have simple way to set members of Active Directory groups.

= API
API is described on http://localhost:8080/#/about (ldapgroups.js/app/partials.html)
