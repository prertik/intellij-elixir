package org.elixir_lang.beam

import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import org.elixir_lang.beam.chunk.Chunk
import org.elixir_lang.beam.chunk.Table
import org.elixir_lang.beam.chunk.debug_info.term.component
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTabbedPane

private fun addTab(tabbedPane: JBTabbedPane, cache: Cache, chunk: Chunk) {
    val typeID = chunk.typeID
    val scrollable: JComponent = when (typeID) {
        Chunk.TypeID.ATOM.toString(), Chunk.TypeID.ATU8.toString() ->
            Table(org.elixir_lang.beam.chunk.atoms.Model(cache.atoms))
        Chunk.TypeID.ATTR.toString() ->
            Table(org.elixir_lang.beam.chunk.keyword.Model(cache.attributes))
        Chunk.TypeID.CINF.toString() ->
            Table(org.elixir_lang.beam.chunk.keyword.Model(cache.compileInfo))
        Chunk.TypeID.CODE.toString() ->
            Table(org.elixir_lang.beam.chunk.code.Model(cache.code))
        Chunk.TypeID.DBGI.toString() ->
            component(cache.debugInfo)
        Chunk.TypeID.EXPT.toString() ->
            Table(org.elixir_lang.beam.chunk.call_definitions.Model(cache.exports))
        Chunk.TypeID.FUNT.toString() ->
            Table(org.elixir_lang.beam.chunk.functions.Model(cache.functions))
        Chunk.TypeID.IMPT.toString() ->
            Table(org.elixir_lang.beam.chunk.imports.Model(cache.imports))
        Chunk.TypeID.LOCT.toString() ->
            Table(org.elixir_lang.beam.chunk.call_definitions.Model(cache.locals))
        Chunk.TypeID.LITT.toString() ->
            Table(org.elixir_lang.beam.chunk.literals.Model(cache.literals))
        Chunk.TypeID.STRT.toString() ->
            Table(org.elixir_lang.beam.chunk.strings.Model(cache.strings))
        else ->
            JPanel()
    }

    tabbedPane.addTab(typeID, JBScrollPane(scrollable))
}

class FileEditor(private val virtualFile: VirtualFile): UserDataHolderBase(), FileEditor {
    private var isActive: Boolean = false

    // GUI
    private lateinit var rootTabbedPane: JBTabbedPane
    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}
    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}

    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? = null
    override fun getComponent(): JComponent {
        rootTabbedPane = JBTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT)

        Beam.from(virtualFile)?.let { beam ->
            val cache = Cache(beam)

            cache.chunkCollection().forEach { chunk ->
                addTab(rootTabbedPane, cache, chunk)
            }
        }

        return rootTabbedPane
    }

    override fun getCurrentLocation(): FileEditorLocation? = null
    override fun getName(): String = "BEAM Chunks"
    override fun getPreferredFocusedComponent(): JComponent? = rootTabbedPane
    override fun isModified(): Boolean = false
    override fun isValid() = true

    override fun selectNotify() {
        isActive = true
//
//        if (isObsolete) {
//            update()
//        }
    }

    override fun deselectNotify() {
        isActive = true
    }

    override fun dispose() {
        Disposer.dispose(this)
    }

    override fun setState(state: FileEditorState) {}
}
