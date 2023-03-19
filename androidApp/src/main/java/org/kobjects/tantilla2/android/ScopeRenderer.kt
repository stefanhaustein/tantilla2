package org.kobjects.tantilla2.android

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.kobjects.tantilla2.android.model.TantillaViewModel
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.scope.AbsoluteRootScope

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RenderScope(viewModel: TantillaViewModel) {
    val scope = viewModel.scope().value

    val definitions = scope.sorted()

    val textWidth = remember {
        mutableStateOf(40)
    }

    key(viewModel.codeUpdateTrigger.value) {
        Scaffold(
            backgroundColor = Color.Transparent,
            topBar = {
                RenderAppBar(viewModel)
            },
            bottomBar = {

            }
        ) {
            LazyColumn(
                Modifier.fillMaxWidth(), contentPadding = PaddingValues(8.dp),
            ) {
                if (viewModel.mode.value == TantillaViewModel.Mode.HIERARCHY || scope.docString.isNotEmpty()) {
                    item {
                        RenderDocumentation(viewModel)
                    }
                }
                for (kind in Definition.Kind.values().toList()  + listOf(null)) {
                    var list: List<Definition>
                    val title: String
                    if (kind == null) {
                        title = "IMPL (defined elsewhere)"
                        if (scope is TraitDefinition) {
                            list = viewModel.userRootScope.traitToClass[scope]?.values?.toSet()
                                ?.toList()
                                ?: emptyList()
                        } else {
                            list = viewModel.userRootScope.classToTrait[scope]?.values?.toSet()?.toList()
                                ?: emptyList()
                        }
                    } else {
                        title = kind.name
                        list =
                            (if (kind == Definition.Kind.PROPERTY && viewModel.mode.value == TantillaViewModel.Mode.HIERARCHY)
                                scope.locals.map { scope[it]!! }
                            else
                                definitions)

                        if (scope == viewModel.systemRootScope) {
                            list = (list + AbsoluteRootScope.iterator().asSequence().toList()).sorted()
                        }

                        list = list.filter { it.kind == kind }
                    }
                    if (list.isNotEmpty()) {
                        item {
                            Text(title, Modifier.padding(0.dp, 4.dp, 0.dp, 0.dp))
                        }
                        for (definition in list) {
                            item(key = scope.name + "::" + definition.name + "::" + definition.hashCode()) {
                                key(viewModel.codeUpdateTrigger.value) {
                                    RenderDefinition(viewModel, definition, textWidth)
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}

