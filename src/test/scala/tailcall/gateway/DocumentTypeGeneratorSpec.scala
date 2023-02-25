package tailcall.gateway

import tailcall.gateway.ast.Document
import tailcall.gateway.remote.Remote
import tailcall.gateway.service._
import zio.schema.DynamicValue
import zio.test.Assertion._
import zio.test._

object DocumentTypeGeneratorSpec extends ZIOSpecDefault {
  override def spec =
    suite("DocumentTypeGenerator")(
      test("document type generation") {
        val document = Document(List(
          Document.Definition.ObjectTypeDefinition(
            "Query",
            List(Document.Definition.FieldDefinition(
              "test",
              List(),
              Document.Type.NamedType("String", false),
              Document.FieldResolver(_ => Remote(DynamicValue("test")))
            ))
          ),
          Document.Definition.SchemaDefinition(Some("Query"), None, None)
        ))
        val out      = document.toGraphQL.map(_.render)
        val expected = """|schema {
                          |  query: Query
                          |}
                          |
                          |type Query {
                          |  test: String
                          |}""".stripMargin
        assertZIO(out)(equalTo(expected))
      },
      test("document with InputValue") {
        val document = Document(List(
          Document.Definition.ObjectTypeDefinition(
            "Query",
            List(Document.Definition.FieldDefinition(
              "test",
              List(
                Document.Definition
                  .InputValueDefinition("arg", Document.Type.NamedType("String", false), Some(DynamicValue("test")))
              ),
              Document.Type.NamedType("String", false),
              Document.FieldResolver(_ => Remote(DynamicValue("test")))
            ))
          ),
          Document.Definition.SchemaDefinition(Some("Query"), None, None)
        ))
        val out      = document.toGraphQL.map(_.render)
        val expected = """|schema {
                          |  query: Query
                          |}
                          |
                          |type Query {
                          |  test(arg: String = "test"): String
                          |}""".stripMargin
        assertZIO(out)(equalTo(expected))
      }
    ).provide(
      DocumentGraphQLGenerator.live,
      DocumentTypeGenerator.live,
      DocumentStepGenerator.live,
      EvaluationRuntime.live,
      EvaluationContext.live
    )
}