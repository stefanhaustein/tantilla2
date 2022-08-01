package org.kobjects.tantilla2.android

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.kobjects.konsole.compose.AnsiConverter
import org.kobjects.tantilla2.android.model.TantillaViewModel
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.classifier.StructDefinition
import org.kobjects.tantilla2.core.runtime.RootScope

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RenderScope(viewModel: TantillaViewModel) {
    val scope = viewModel.scope().value

    val definitions = scope.definitions.iterator().asSequence().toList().sortedBy { it.name }

    Scaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            if (viewModel.mode.value == TantillaViewModel.Mode.HELP) {
                RenderAppBar(viewModel, if (scope.parentScope == null) viewModel.fileName.value else scope.name)
            } else {
                RenderAppBar(viewModel, if (scope.parentScope == RootScope) viewModel.fileName.value else  scope.name, "Add" to {viewModel.edit(scope, null)})
            } },
    ) {
        LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(8.dp)) {
            for (kind in Definition.Kind.values().toList() /* + listOf(null) */) {
                val list: List<Definition>
                val title: String
                if (kind == null) {
                    title = "IMPL (defined elsewhere)"
                    if (scope is StructDefinition) {
                        list = viewModel.compilationResults.value.classToTrait[scope]?.values?.toList() ?: emptyList()
                    } else if (scope is TraitDefinition) {
                        list = viewModel.compilationResults.value.traitToClass[scope]?.values?.toList() ?: emptyList()
                    } else {
                        continue
                    }
                } else {
                    title = kind.name
                    list = (if (kind == Definition.Kind.PROPERTY)
                        scope.definitions.locals.map { scope.definitions[it]!! }
                    else
                        definitions).filter { it.kind == kind }
                }
                    if (list.isNotEmpty()) {
                        item {
                            Text(title, Modifier.padding(0.dp, 4.dp, 0.dp, 0.dp))
                        }
                        for (definition in list) {
                            item(key = scope.name + "::" + definition.name) {
                                RenderDefinition(viewModel, definition)
                            }
                        }
                    }

            }
        }
    }
}


@Composable
fun RenderDefinition(viewModel: TantillaViewModel, definition: Definition) {
    Card(
        backgroundColor = if (viewModel.compilationResults.value.definitionsWithErrors.contains(definition)
            || viewModel.withRuntimeException.containsKey(definition)) Color(0xffff8888L) else Color(0xffeeeeee),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .padding(4.dp)
            .pointerInput(Unit) {
                val editable =
                    !definition.isScope() && viewModel.mode.value == TantillaViewModel.Mode.HIERARCHY
                detectTapGestures(
                    onLongPress = {
                        if (definition.isScope()) {
                            viewModel.scope().value = definition.getValue(null) as Scope
                        } else if (editable) {
                            viewModel.edit(definition.parentScope!!, definition)
                        }
                    },
                    onTap = {
                        if (viewModel.expanded.value.contains(definition)) {
                            viewModel.expanded.value -= definition
                        } else {
                            viewModel.expanded.value += definition
                        }
                    }
                )
            }
    ) {
        Box(Modifier.padding(8.dp)) {
            val expanded = viewModel.expanded.value.contains(definition)
            val help = viewModel.mode.value == TantillaViewModel.Mode.HELP
            Column() {
                val writer = CodeWriter(highlighting = CodeWriter.defaultHighlighting)
                if (!expanded) {
                    definition.serializeTitle(writer)
                } else {
                    definition.serializeSummary(writer)
                }
                Text(AnsiConverter.ansiToAnnotatedString(writer.toString()))
            }
            Row(modifier = Modifier
                .align(Alignment.TopEnd)
                .alpha(0.2f)) {
                if (definition.isScope() || !help) {
                    Icon(
                        Icons.Default.Fullscreen,
                        contentDescription = "Open",
                        modifier = Modifier.clickable {
                            if (definition.isScope()) {
                                viewModel.scope().value = definition.getValue(null) as Scope
                            } else {
                                viewModel.edit(definition.parentScope!!, definition)
                            }
                        })
                }
            }
        }
    }
}
