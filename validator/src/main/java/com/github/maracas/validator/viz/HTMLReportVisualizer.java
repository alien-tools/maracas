package com.github.maracas.validator.viz;

import static j2html.TagCreator.a;
import static j2html.TagCreator.attrs;
import static j2html.TagCreator.b;
import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h5;
import static j2html.TagCreator.head;
import static j2html.TagCreator.hr;
import static j2html.TagCreator.html;
import static j2html.TagCreator.input;
import static j2html.TagCreator.join;
import static j2html.TagCreator.label;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.maracas.validator.accuracy.AccuracyAnalyzer;
import com.github.maracas.validator.accuracy.AccuracyCase;

import j2html.tags.DomContent;
import j2html.tags.specialized.HtmlTag;

/**
 * HTML eeport visualizer of accuracy metrics
 */
public class HTMLReportVisualizer extends ReportVisualizer {
    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(HTMLReportVisualizer.class);

    /**
     * Title of the HTML report
     */
    private static final String REPORT_TITLE = "Maracas Validator Report";

    /**
     * Accuracy metrics analyzer
     */
    private final AccuracyAnalyzer analyzer;

    /**
     * Collection of false positive cases
     */
    private final Collection<AccuracyCase> falsePositives;

    /**
     * Collection of false negatives cases
     */
    private final Collection<AccuracyCase> falseNegatives;

    /**
     * Collection of true positive cases
     */
    private final Collection<AccuracyCase> truePositives;

    /**
     * Creates a HTMLReportVisualizer instance.
     *
     * @param cases collection of {@link AccuracyCase} instances
     * @param path  path where the report must be generated
     */
    public HTMLReportVisualizer(Collection<AccuracyCase> cases, Path path) {
        super(cases, path);
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
            logger.error("Cannot generate the HTML report:", e);
        }
    }

    /**
     * Generates the accuracy metrics view. Includes information about precision,
     * recall, and number of false positives, false negatives, and true positives.
     *
     * @return div with the accuracy metrics view
     */
    private DomContent generateMetricsView() {
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

    /**
     * Generates the false positives view. Contains information about reported
     * broken uses that have not been matched against any compiler message.
     *
     * @return div with the false positives view
     */
    private DomContent generateFalsePositivesView() {
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
                        attrs(".list-group-item.list-group-item-action"),
                        div(
                            attrs(".form-check.mt-2.mb-4"),
                            input(attrs(".form-check-input"))
                                .withType("checkbox"),
                            label(attrs(".form-check-label"))
                                .withText("Reviewed")
                        ),
                        p(
                        	join(b("ID:"), c.id().toString()),
                        	br(),
                            join(
                                b("Path:"),
                                a()
                                    .withHref(c.brokenUse().element().getPosition().getFile().getAbsolutePath())
                                    .withText(c.brokenUse().element().getPosition().getFile().getAbsolutePath())
                            ),
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

    /**
     * Generates the false negatives view. Contains information about reported
     * compiler messages that have not been matched against any broken use.
     *
     * @return div with the false negatives view
     */
    private DomContent generateFalseNegativesView() {
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
                        attrs(".list-group-item.list-group-item-action"),
                        div(
                            attrs(".form-check.mt-2.mb-4"),
                            input(attrs(".form-check-input"))
                                .withType("checkbox"),
                            label(attrs(".form-check-label"))
                                .withText("Reviewed")
                        ),
                        p(
                        	join(b("ID:"), c.id().toString()),
                        	br(),
                            join(
                                b("Path:"),
                                a()
                                .withHref(c.messages().get(0).path())
                                .withText(c.messages().get(0).path())
                            ),
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

    /**
     * Generates the true positives view. Contains information about matched
     * broken uses and compiler messages.
     *
     * @return div with the true positives view
     */
    private DomContent generateTruePositivesView() {
        return div(
            attrs(".p-4"),
            h2("Matched Cases (True Positives)"),
            div(
                attrs(".input-group.my-2"),
                input()
                    .withClass("form-control")
                    .withId("maracas-tp-input")
                    .attr("onkeyup", "filter(\"maracas-tp-input\", \"maracas-tp-ul\")")
                    .withPlaceholder("Filter matched cases...")
                    .withType("text")
            ),
            ul(
                attrs("#maracas-tp-ul.list-group.maracas-scroll.my-2.border.border-2.border-dark"),
                each(truePositives, c ->
                    li(
                        attrs(".list-group-item.list-group-item-action"),
                        div(
                            attrs(".row"),
                            div(
                                attrs(".col-6.p-4"),
                                div(
                                    attrs(".form-check.mt-2.mb-4"),
                                    input(attrs(".form-check-input"))
                                        .withType("checkbox"),
                                    label(attrs(".form-check-label"))
                                        .withText("Reviewed")
                                ),
                                h5("Broken use:"),
                                p(
                                	join(b("ID:"), c.id().toString()),
                                	br(),
                                    join(
                                        b("Path:"),
                                        a()
                                            .withHref(c.brokenUse().element().getPosition().getFile().getAbsolutePath())
                                            .withText(c.brokenUse().element().getPosition().getFile().getAbsolutePath())
                                    ),
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
                            ),
                            div(
                                attrs(".col-6.p-4"),
                                h5("Compiler messages:"),
                                ul(
                                    attrs(".list-group.my-2"),
                                    each(c.messages(), m ->
                                        li(
                                            attrs(".list-group-item"),
                                            p(
                                                join(b("Path:"),
                                                    a()
                                                    .withHref(m.path())
                                                    .withText(m.path())
                                                ),
                                                br(),
                                                join(b("Line:"), String.valueOf(m.line())),
                                                br(),
                                                join(b("Message:"), String.valueOf(m.message()))
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    /**
     * Generates a JavaScript script to filter out list items based on a search
     * string.
     *
     * @return script with the JavaScript filter() function
     */
    private DomContent generateScript() {
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
