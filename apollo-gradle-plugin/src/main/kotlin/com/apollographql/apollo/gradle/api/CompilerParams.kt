package com.apollographql.apollo.gradle.api

import com.apollographql.apollo.api.ApolloExperimental
import com.apollographql.apollo.compiler.OperationIdGenerator
import com.apollographql.apollo.compiler.OperationOutputGenerator
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/**
 * CompilerParams contains all the parameters needed to invoke the apollo compiler.
 *
 * The setters are present for backward compatibility with kotlin build scripts and will go away
 * in a future release.
 */
interface CompilerParams {
  /**
   * Whether to generate Java or Kotlin models
   *
   * Default value: false
   */
  val generateKotlinModels: Property<Boolean>

  /**
   * Warn if using a deprecated field
   *
   * Default value: true
   */
  val warnOnDeprecatedUsages: Property<Boolean>

  /**
   * Fail the build if there are warnings. This is not named `allWarningAsErrors` to avoid nameclashes with the Kotlin options
   *
   * Default value: false
   */
  val failOnWarnings: Property<Boolean>

  /**
   * Whether to generate operationOutput.json. operationOutput.json contains information such as
   * operation id, name and complete source sent to the server. This can be used to upload
   * a query's exact content to a server that doesn't support automatic persisted queries.
   *
   * The operation output is written in [CompilationUnit.operationOutputFile]
   *
   * Default value: false
   */
  val generateOperationOutput: Property<Boolean>

  /**
   * For custom scalar types like Date, map from the GraphQL type to the jvm/kotlin type.
   *
   * Default value: the empty map
   */
  val customTypeMapping: MapProperty<String, String>

  /**
   * By default, Apollo uses `Sha256` hashing algorithm to generate an ID for the query.
   * To provide a custom ID generation logic, pass an `instance` that implements the [OperationIdGenerator]. How the ID is generated is
   * indifferent to the compiler. It can be an hashing algorithm or generated by a backend.
   *
   * Example Md5 hash generator:
   * ```groovy
   * import com.apollographql.apollo.compiler.OperationIdGenerator
   *
   * apollo {
   *   operationIdGenerator = new OperationIdGenerator() {
   *     String apply(String operationDocument, String operationFilepath) {
   *       return operationDocument.md5()
   *     }
   *
   *     /**
   *      * Use this version override to indicate an update to the implementation.
   *      * This invalidates the current cache.
   *      */
   *     String version = "v1"
   *   }
   * }
   * ```
   *
   * Default value: [OperationIdGenerator.Sha256]
   */
  val operationIdGenerator: Property<OperationIdGenerator>

  /**
   * A generator to generate the operation output from a list of operations.
   * OperationOutputGenerator is similar to [OperationIdGenerator] but can work on lists. This is useful if you need
   * to register/whitelist your operations on your server all at once.
   *
   * Example Md5 hash generator:
   * ```groovy
   * import com.apollographql.apollo.compiler.OperationIdGenerator
   *
   * apollo {
   *   operationOutputGenerator = new OperationIdGenerator() {
   *     String apply(List<operation operationDocument, String operationFilepath) {
   *       return operationDocument.md5()
   *     }
   *
   *     /**
   *      * Use this version override to indicate an update to the implementation.
   *      * This invalidates the current cache.
   *      */
   *     String version = "v1"
   *   }
   * }
   * ```
   *
   * Default value: [OperationIdGenerator.Sha256]
   */
  val operationOutputGenerator: Property<OperationOutputGenerator>

  /**
   * The custom types code generate some warnings that might make the build fail.
   * suppressRawTypesWarning will add the appropriate SuppressWarning annotation
   *
   * Default value: false
   */
  val suppressRawTypesWarning: Property<Boolean>

  /**
   * When true, the generated classes names will end with 'Query' or 'Mutation'.
   * If you write `query droid { ... }`, the generated class will be named 'DroidQuery'.
   *
   * Default value: true
   */
  val useSemanticNaming: Property<Boolean>

  /**
   * The nullable value type to use. One of: "annotated", "apolloOptional", "guavaOptional", "javaOptional", "inputType"
   *
   * Default value: "annotated"
   * Only valid for java models as kotlin has proper nullability support
   */
  val nullableValueType: Property<String>

  /**
   * Whether to generate builders for java models
   *
   * Default value: false
   * Only valid for java models as kotlin has data classes
   */
  val generateModelBuilder: Property<Boolean>

  /**
   * When true, java beans getters and setters will be generated for fields. If you request a field named 'user', the generated
   * model will have a `getUser()` property instead of `user()`
   *
   * Default value: false
   * Only valid for java as kotlin has properties
   */
  val useJavaBeansSemanticNaming: Property<Boolean>

  /**
   * Apollo Gradle plugin supports generating visitors for compile-time safe handling of polymorphic datatypes.
   * Enabling this requires source/target compatibility with Java 1.8.
   *
   * Default value: false
   */
  val generateVisitorForPolymorphicDatatypes: Property<Boolean>

  /**
   * The package name of the models is computed from their folder hierarchy like for java sources.
   *
   * If you want, you can prepend a custom package name here to namespace your models.
   *
   * Default value: the empty string
   */
  val rootPackageName: Property<String>

  /**
   * The graphql files containing the queries.
   *
   * By default, the plugin will use [Service.sourceFolder] to populate the graphqlSourceDirectorySet with all the matching .graphql or .gql files.
   * You can change this behaviour by calling `graphqlSourceDirectorySet.srcDir("path/to/your/directory")` and specifying includes/excludes:
   * graphqlSourceDirectorySet.srcDir("path/to/your/directory")
   * graphqlSourceDirectorySet.include("**&#47;*.graphql")
   * graphqlSourceDirectorySet.exclude("**&#47;schema.graphql")
   *
   * It is an error to call `include` or `exclude` without calling `srcDir`
   *
   * Directories set on [ApolloExtension.graphqlSourceDirectorySet] or [Service.graphqlSourceDirectorySet] will not be used for test
   * variants as that would produce duplicate classes since the exact same files would be compiled for the main variants.
   */
  val graphqlSourceDirectorySet: SourceDirectorySet

  /**
   * The schema file
   *
   * By default, it will use [Service.schemaPath] to set schemaFile.
   * You can override it from [ApolloExtension.onCompilationUnit] for more advanced use cases
   */
  val schemaFile: RegularFileProperty

  /**
   * Whether to generate Kotlin models with `internal` visibility modifier.
   *
   * Default value: false
   */
  val generateAsInternal: Property<Boolean>

  /**
   * A list of [Regex] patterns for GraphQL enums that should be generated as Kotlin sealed classes instead of the default Kotlin enums.
   *
   * Use this if you want your client to have access to the rawValue of the enum. This can be useful if new GraphQL enums are added but
   * the client was compiled against an older schema that doesn't have knowledge of the new enums.
   */
  val sealedClassesForEnumsMatching: ListProperty<String>

  /**
   * Whether or not to generate Apollo metadata. Apollo metadata is used for multi-module support. Set this to true if you want other
   * modules to be able to re-use fragments and types from this module.
   *
   * This is currently experimental and this API might change in the future.
   *
   * Default value: false
   */
  @ApolloExperimental
  val generateApolloMetadata: Property<Boolean>

  /**
   * A list of [Regex] patterns for input/scalar/enum types that should be generated whether or not they are used by queries/fragments
   * in this module. When using multiple modules, Apollo Android will generate all the types by default in the root module
   * because the root module doesn't know what types are going to be used by dependent modules. This can be prohibitive in terms
   * of compilation speed for large projects. If that's the case, opt-in the types that are used by multiple dependent modules here.
   * You don't need to add types that are used by a single dependent module.
   *
   * This is currently experimental and this API might change in the future.
   *
   * Default value: if (generateApolloMetadata) listOf(".*") else listOf()
   */
  @ApolloExperimental
  val alwaysGenerateTypesMatching: SetProperty<String>

  /**
   * Use the given package name and does not take into account the folders hierarchy.
   *
   * - Operations will be in `packageName`
   * - Fragments will be in `packageName.fragment`
   * - Input/Enum/Scalar types are not handled yet and will continue to be in the schemaPackageName
   *
   * This is currently experimental and this API might change in the future.
   *
   * Default value: false
   */
  @ApolloExperimental
  val packageName: Property<String>
}