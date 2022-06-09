package schematransformer.sparql

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.impl.BooleanLiteral
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.eclipse.rdf4j.model.vocabulary.SHACL
import org.eclipse.rdf4j.model.vocabulary.SKOS
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection
import org.eclipse.rdf4j.sail.memory.model.IntegerMemLiteral
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions
import org.eclipse.rdf4j.sparqlbuilder.core.Dataset
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
import schematransformer.type.NodeShape
import schematransformer.type.PropertyShape
import schematransformer.vocabulary.DXPROFILE

object SPARQLQueries {
    private fun from(vararg context: IRI): String = context.joinToString("\n") { "FROM <$it>" }

    fun getProfileResources(conn: SailRepositoryConnection, vararg context: IRI): Map<Value, List<Value>> {
        val contexts = Dataset().from(*context)
        val role = SparqlBuilder.`var`("role")
        val artifact = SparqlBuilder.`var`("artifact")
        val resource = SparqlBuilder.`var`("resource")
        val prof = SparqlBuilder.`var`("prof")

        val q = Queries.SELECT(role, artifact)
            .prefix(SHACL.NS)
            .prefix(DXPROFILE.NS)
            .from(contexts)
            .where(
                prof.isA(DXPROFILE.PROFILE)
                    .andHas(DXPROFILE.HASRESOURCE, resource)
                    .and(
                        resource.has(DXPROFILE.HASROLE, role)
                            .andHas(DXPROFILE.HASARTIFACT, artifact)
                    )
            )

        return conn.prepareTupleQuery(q.queryString).evaluate()  // Improve.
            .groupBy(
                { it.getValue("role") },
                { it.getValue("artifact") })
    }

    fun getRootObjectIRI(conn: SailRepositoryConnection, vararg context: IRI): IRI? {
        val contexts = Dataset().from(*context)
        val root = SparqlBuilder.`var`("root")

        val q = Queries.SELECT(root)
            .prefix(SHACL.NS)
            .from(contexts)
            .where(
                root
                    .isA(SHACL.NODE_SHAPE)
                    .andHas(RDFS.COMMENT, "RootObject")
            )

        return conn.prepareTupleQuery(q.queryString).evaluate().firstOrNull()?.getValue("root") as IRI?
    }

    fun getNodeShape(
        conn: SailRepositoryConnection,
        nodeShapeIRI: IRI,
        vararg context: IRI
    ): NodeShape {
        val contexts = Dataset().from(*context)
        val targetClass = SparqlBuilder.`var`("targetClass")
        val label = SparqlBuilder.`var`("label")
        val comment = SparqlBuilder.`var`("comment")
        val enum = SparqlBuilder.`var`("enum")
        val property = SparqlBuilder.`var`("property")
        val propPath = SparqlBuilder.`var`("propPath")
        val propRangeType = SparqlBuilder.`var`("propRangeType")
        val propIsNode = SparqlBuilder.`var`("propIsNode")
        val propLabel = SparqlBuilder.`var`("propLabel")
        val propComment = SparqlBuilder.`var`("propComment")
        val propMinCount = SparqlBuilder.`var`("propMinCount")
        val propMaxCount = SparqlBuilder.`var`("propMaxCount")
        val nodeShape = Iri { nodeShapeIRI.stringValue() }

        val q = Queries.SELECT()
            .distinct()
            .prefix(RDF.NS)
            .prefix(RDFS.NS)
            .prefix(SHACL.NS)
            .prefix(SKOS.NS)
            .from(contexts)
            .where(
                GraphPatterns.tp(nodeShapeIRI, SHACL.TARGET_CLASS, targetClass)
                    .and(
                        targetClass
                            .has({ path -> path.pred(RDFS.LABEL).or(SKOS.PREF_LABEL) }, label)
                            .andHas({ path -> path.pred(RDFS.COMMENT).or(SKOS.DEFINITION) }, comment)
                    )
                    .and(
                        GraphPatterns.optional(
                            nodeShape.has({ p ->
                                p.pred(SHACL.IN)
                                    .then(RDF.REST).zeroOrMore()
                                    .then(RDF.FIRST)
                                    .group().oneOrMore()
                            }, enum)
                        )
                    ).and(
                        GraphPatterns.optional(
                            GraphPatterns.union(
                                nodeShape.has(SHACL.PROPERTY, property),
                                nodeShape.has(
                                    { p ->
                                        p.pred(SHACL.AND)
                                            .then(RDF.REST).zeroOrMore()
                                            .then(RDF.FIRST)
                                            .then(SHACL.PROPERTY)
                                            .group().oneOrMore()
                                    },
                                    property
                                )
                            ).and(
                                property
                                    .has(SHACL.PATH, propPath)
                                    .andHas({ p -> p.pred(SHACL.DATATYPE).or(SHACL.NODE) }, propRangeType)
                            ).and(
                                /* WIP. */
                                Expressions.bind(
                                    Expressions.exists(
                                        Rdf.literalOf(
                                            !property.has(SHACL.NODE, propRangeType)
                                                .filterExists()
                                                .isEmpty()
                                        )
                                    ), propIsNode
                                )
                                /* WIP. */
                            )
                        )
                    )
            )

        println(q.queryString)
        println()
        throw Exception()
        val r = """
            PREFIX ${SHACL.PREFIX}: <${SHACL.NAMESPACE}>
            PREFIX ${SKOS.PREFIX}: <${SKOS.NAMESPACE}>
            SELECT DISTINCT *
            ${from(*context)}
            WHERE {
                <$nodeShapeIRI> sh:targetClass ?targetClass .
                
                ?targetClass rdfs:label|skos:prefLabel ?label ;
                             rdfs:comment|skos:definition ?comment .
                             
                OPTIONAL { <$nodeShapeIRI> (sh:in/rdf:rest*/rdf:first)+ ?enum }
                
                OPTIONAL {
                    { <$nodeShapeIRI> sh:property ?property }
                    UNION
                    { <$nodeShapeIRI> (sh:and/rdf:rest*/rdf:first/sh:property)+ ?property }
                    
                    ?property sh:path ?propPath ;
                              sh:datatype|sh:node ?propRangeType .
                    BIND(EXISTS { ?property sh:node ?propRangeType } AS ?propIsNode)
                    OPTIONAL { ?propPath rdfs:label|skos:prefLabel ?propLabel . }
                    OPTIONAL { ?propPath rdfs:comment|skos:definition ?propComment . }
                    OPTIONAL { ?property sh:minCount ?propMinCount . }
                    OPTIONAL { ?property sh:maxCount ?propMaxCount . }
                }
            }
    """.trimIndent()

        return conn.prepareTupleQuery(q.queryString).evaluate()
            .map { row ->
                row
                    .associate { it.name to it.value }
                    .filter { it.value != null }
            }.let { results ->
                NodeShape(targetClass = results[0]["targetClass"] as IRI,
                    label = results[0]["label"]?.stringValue(),
                    comment = results[0]["comment"]?.stringValue(),
                    `in` = results.mapNotNull { it["enum"] as? IRI },
                    properties = results.filter { it["property"] != null }.associate {
                        it["property"]!!.stringValue() to PropertyShape(
                            path = it["propPath"] as IRI,
                            node = (if ((it["propIsNode"] as BooleanLiteral).booleanValue())
                                it["propRangeType"] as IRI
                            else null),
                            datatype = (if (!(it["propIsNode"] as BooleanLiteral).booleanValue())
                                it["propRangeType"] as IRI
                            else null),
                            label = it["propLabel"]?.stringValue(),
                            comment = it["propComment"]?.stringValue(),
                            minCount = (it["propMinCount"] as? IntegerMemLiteral)?.intValue(),
                            maxCount = (it["propMaxCount"] as? IntegerMemLiteral)?.intValue(),
                            `in` = null,
                        )
                    }
                )
            }
    }
}