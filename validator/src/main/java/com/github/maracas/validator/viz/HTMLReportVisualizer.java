package com.github.maracas.validator.viz;

import static j2html.TagCreator.attrs;
import static j2html.TagCreator.b;
import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.head;
import static j2html.TagCreator.hr;
import static j2html.TagCreator.html;
import static j2html.TagCreator.input;
import static j2html.TagCreator.join;
import static j2html.TagCreator.li;
import static j2html.TagCreator.link;
import static j2html.TagCreator.p;
import static j2html.TagCreator.script;
import static j2html.TagCreator.style;
import static j2html.TagCreator.title;
import static j2html.TagCreator.ul;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Collection;

import com.github.maracas.validator.accuracy.AccuracyAnalyzer;
import com.github.maracas.validator.accuracy.AccuracyCase;

import j2html.tags.DomContent;
import j2html.tags.specialized.HtmlTag;

/**
 *
 */
public class HTMLReportVisualizer extends ReportVisualizer {

    private final AccuracyAnalyzer analyzer;
    private final Collection<AccuracyCase> falsePositives;
    private final Collection<AccuracyCase> falseNegatives;
    private final Collection<AccuracyCase> truePositives;

    private static final String REPORT_TITLE = "Maracas Validator Report";

    public HTMLReportVisualizer(Collection<AccuracyCase> cases, Path report) {
        super(cases, report);
        this.analyzer = new AccuracyAnalyzer(cases);
        this.falsePositives = analyzer.falsePositives();
        this.falseNegatives = analyzer.falseNegatives();
        this.truePositives = analyzer.truePositives();
    }

    @Override
    public void generate() {
        try (Writer writer = new FileWriter(path.toFile())) {
            HtmlTag html = html(
                head(
                    title(REPORT_TITLE),
                    style(".maracas-scroll { height: 70vh; overflow: scroll; }"),
                    link()
                        .withRel("stylesheet")
                        .withHref("https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css")
                        .attr("integrity", "sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC")
                        .attr("crossorigin", "anonymous")
                ),
                body(
                    div(
                        attrs(".container-fluid"),
                        div(
                            attrs(".text-center.my-4"),
                            h1(REPORT_TITLE)
                        ),
                        div(
                            attrs(".row"),
                            div(
                                attrs(".col"),
                                div(
                                    attrs(".row"),
                                    hr(),
                                    generateMetricsView(),
                                    hr(),
                                    generateFalsePositivesView(),
                                    generateFalseNegativesView(),
                                    hr(),
                                    generateTruePositivesView()
                                )
                            )
                        )
                    ),
                    script()
                        .withSrc("https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js")
                        .attr("integrity", "sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM")
                        .attr("crossorigin", "anonymous"),
                    generateScript()
                )
            );
            writer.write(html.render());
        } catch (IOException e) {

        }
    }

    public DomContent generateMetricsView() {
        return div(
            attrs(".col-6.p-4"),
            h2("Accuracy Metrics"),
            div(
                attrs(".my-2"),
                p(
                    join(b("Precision:"), String.valueOf(analyzer.precision())),
                    br(),
                    join(b("Recall:"), String.valueOf(analyzer.recall())),
                    br(),
                    join(b("True positives:"), String.valueOf(truePositives.size())),
                    br(),
                    join(b("False positives:"), String.valueOf(falsePositives.size())),
                    br(),
                    join(b("False negatives:"), String.valueOf(falseNegatives.size()))
                )
            )
        );
    }

    public DomContent generateFalsePositivesView() {
        return div(
            attrs(".col-6.p-4"),
            h2("Broken Uses (False Positives)"),
            div(
                attrs(".input-group.my-2"),
                input()
                    .withClass("form-control")
                    .withId("maracas-fp-input")
                    .attr("onkeyup", "filter(\"maracas-fp-input\", \"maracas-fp-ul\")")
                    .withPlaceholder("Filter broken uses...")
                    .withType("text")
            ),
            ul(
                attrs("#maracas-fp-ul.list-group.maracas-scroll.my-2.border.border-2.border-dark"),
                each(falsePositives, c ->
                    li(
                        attrs(".list-group-item"),
                        p(
                            join(b("Path:"), c.brokenUse().element().getPosition().getFile().getAbsolutePath()),
                            br(),
                            join(b("Line:"), String.valueOf(c.brokenUse().element().getPosition().getLine())),
                            br(),
                            join(b("Breaking change:"), c.brokenUse().change().toString()),
                            br(),
                            join(b("API use:"), c.brokenUse().use().toString()),
                            br(),
                            join(b("Used declaration:"), c.brokenUse().usedApiElement().toString()),
                            br(),
                            join(b("Source declaration:"), c.brokenUse().source().toString())
                        )
                    )
                )
            )
        );
    }

    public DomContent generateFalseNegativesView() {
        return div(
            attrs(".col-6.p-4"),
            h2("Compiler Messages (False Negatives)"),
            div(
                attrs(".input-group.my-2"),
                input()
                    .withClass("form-control")
                    .withId("maracas-fn-input")
                    .attr("onkeyup", "filter(\"maracas-fn-input\", \"maracas-fn-ul\")")
                    .withPlaceholder("Filter compiler messages...")
                    .withType("text")
            ),
            ul(
                attrs("#maracas-fn-ul.list-group.maracas-scroll.my-2.border.border-2.border-dark"),
                each(falseNegatives, c ->
                    li(
                        attrs(".list-group-item"),
                        p(
                            join(b("Path:"), c.messages().get(0).path()),
                            br(),
                            join(b("Line:"), String.valueOf(c.messages().get(0).line())),
                            br(),
                            join(b("Message:"), String.valueOf(c.messages().get(0).message()))
                        )
                    )
                )
            )
        );
    }

    public DomContent generateTruePositivesView() {
        return div(
            attrs(".p-4"),
            h2("Matched Cases (True Positives)"),
            div(
                attrs(".input-group.my-2"),
                input()
                    .withClass("form-control")
                    .withId("maracas-fp-input")
                    .attr("onkeyup", "filter(\"maracas-tp-input\", \"maracas-tp-ul\")")
                    .withPlaceholder("Filter matched cases...")
                    .withType("text")
            ),
            ul(
                attrs("#maracas-fp-ul.list-group.maracas-scroll.my-2"),
                each(truePositives, c ->
                    li(
                        attrs(".list-group-item"),
                        p(
                            join(b("Path:"), c.brokenUse().element().getPosition().getFile().getAbsolutePath()),
                            br(),
                            join(b("Line:"), String.valueOf(c.brokenUse().element().getPosition().getLine())),
                            br(),
                            join(b("Breaking change:"), c.brokenUse().change().toString()),
                            br(),
                            join(b("API use:"), c.brokenUse().use().toString()),
                            br(),
                            join(b("Used declaration:"), c.brokenUse().usedApiElement().toString()),
                            br(),
                            join(b("Source declaration:"), c.brokenUse().source().toString())
                        )
                    )
                )
            )
        );
    }

    public DomContent generateScript() {
        String script = """
            function filter(inputId, ulId) {
                var input = document.getElementById(inputId);
                var filter = input.value.toUpperCase();
                var ul = document.getElementById(ulId);
                var li = ul.getElementsByTagName("li");

                for (i = 0; i < li.length; i++) {
                    var p = li[i].getElementsByTagName("p")[0];
                    var val = p.textContent || p.innerText;
                    if (val.toUpperCase().indexOf(filter) > -1) {
                        li[i].style.display = "";
                    } else {
                        li[i].style.display = "none";
                    }
                }
            }
            """;
        return script(script);
    }
}
