JCD Presentation
================

This module hosts the presentation of concepts behind the project.

## Building and Viewing

The presentation described in `asciidoc` format using the `reveal.js` backend.
Main source file is `src/main/asciidoc/presentation.adoc`.

Use `mvn package` to build the presentation in html format.
The file together with all necessary javascript modules will be in the `target/generated-slides`
directory.

Open the presentation html file in browser to display it.

### Render the presentation to a .pdf file

Open the presentation in a browser (Chrome is preferable).
Add `?print-pdf` to the URL.
The browser will render the presentation in a paged format.
After that use standard browsers print function to save presentation to PDF.

## References

[Reveal.js](https://revealjs.com/#/) - javascript library for presentation.
Used by `asciidoctor` backend to render slides.

[Asciidoctor Reveal.js](https://asciidoctor.org/docs/asciidoctor-revealjs/) - plugin for asciidoctor.
The plugins provides a backend that is capable to render document as slides.

[Asciidoctor](https://asciidoctor.org/) - utility to render documents in `asciidoc`
format into various document types.

[Asciidoctor maven plugin](https://asciidoctor.org/docs/asciidoctor-maven-plugin/) - maven plugin to
process asciidoc documents.
