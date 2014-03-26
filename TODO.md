# To Do

The following are a list of planned enhancements for this project:

## API

- Use an RDF vocabulary to describe operation mixes in addition to existing formats
- `RdfProgressListener`
    - Generate an RDF form output, define a vocabulary for this
    - Add a `--rdf` flag to set this file for the `benchmark` command
- Lag Compensation
    - Try to calculate how much delay in HTTP communications there is with the endpoint via simple GETs to the endpoint
    - Adjust all values for the endpoint by this delay
- XSLT Stylesheets for rendering XML results in usable form in browsers
- In-memory operation support
    - Some basic operations have been added but more could be supported