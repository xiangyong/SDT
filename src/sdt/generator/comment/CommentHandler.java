package sdt.generator.comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.texteditor.ITextEditor;

import sdt.core._;

public class CommentHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String f = "";
		// 获取 EditorPart
		IEditorPart ep = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		// 强转会出问题
		ITextEditor te = (ITextEditor) ep;

		IDocument doc = te.getDocumentProvider().getDocument(te.getEditorInput());
		String txt = doc.get();

		final ASTParser astParser = ASTParser.newParser(AST.JLS3);
		astParser.setSource(txt.toCharArray());
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		CompilationUnit cu = (CompilationUnit) (astParser.createAST(null));
		AST ast = cu.getAST();
		// 第一次遍历
		SrcVisitor d = new SrcVisitor(null);
		cu.accept(d);
		StringBuffer query = new StringBuffer();
		String[] words = d.fFields.toArray(new String[0]);
		for (int i = 0; i < words.length; i++) {
			if (i != 0)
				query.append("!");
			query.append(words[i]);
		}
		String cnString = _.en2cn(query.toString());
		String[] cns = cnString.split("！|。");
		Map<String, String> dic = new HashMap<String, String>();
		for (int i = 0; i < cns.length; i++) {
			dic.put(words[i], cns[i]);
		}
		// 第二次遍历
		d.fAst = ast;
		d.fDic = dic;
		try {
			cu.recordModifications();
			cu.accept(d);
			TextEdit edit = cu.rewrite(doc, JavaCore.getOptions());
			edit.apply(doc);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		return null;
	}

	class SrcVisitor extends ASTVisitor {

		public Set<String> fFields = new HashSet<String>();
		public AST fAst;
		public Map<String, String> fDic;

		public SrcVisitor(AST ast) {
			this.fAst = ast;
		}

		@Override
		public boolean visit(FieldDeclaration node) {
			if (node.getJavadoc() != null)
				return true;

			String words = getWords(((VariableDeclarationFragment) node.fragments().get(0)).getName()
					.getFullyQualifiedName());
			if (fAst != null) {
				String cn = fDic.get(words);
				if (cn == null)
					return true;
				Javadoc jd = fAst.newJavadoc();
				TagElement tag = fAst.newTagElement();
				TextElement txt = fAst.newTextElement();
				tag.fragments().add(txt);
				txt.setText(cn);

				jd.tags().add(tag);

				node.setJavadoc(jd);
			} else {
				fFields.add(words);
			}
			return true;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean visit(MethodDeclaration node) {
			if (node.getJavadoc() != null)
				return true;

			String words = getWords(node.getName().getFullyQualifiedName());
			List<SingleVariableDeclaration> list = node.parameters();
			if (fAst != null) {
				String cn = fDic.get(words);
				if (cn == null)
					return true;

				List<TagElement> tes = new ArrayList<TagElement>();
				TagElement tag = fAst.newTagElement();
				tag.setTagName("<b>" + node.getName().getFullyQualifiedName() + "</b>\t");
				TextElement txt = fAst.newTextElement();
				tag.fragments().add(txt);
				txt.setText(cn);
				tes.add(tag);
				for (SingleVariableDeclaration var : list) {
					cn = fDic.get(getWords(var.getName().getFullyQualifiedName()));
					if (cn == null)
						return true;
					tag = fAst.newTagElement();
					tag.setTagName("@param " + var.getName().getFullyQualifiedName() + "\t");
					txt = fAst.newTextElement();
					tag.fragments().add(txt);
					txt.setText(cn);
					tes.add(tag);
				}
				Javadoc jd = fAst.newJavadoc();
				jd.tags().addAll(tes);
				node.setJavadoc(jd);
			} else {
				fFields.add(words);
				for (SingleVariableDeclaration var : list) {
					fFields.add(getWords(var.getName().getFullyQualifiedName()));
				}
			}
			return true;
		}

		private String getWords(String s) {
			s = s.replace('_', ' ');
			s = s.replace('>', ' ');
			s = s.replaceAll("<", " of ");
			StringBuffer f = new StringBuffer(s);
			char c;
			for (int i = 0; i < f.length(); i++) {
				c = f.charAt(i);
				if (Character.isUpperCase(c) && i > 0 && Character.isLowerCase(f.charAt(i - 1))) {
					f.insert(i, " ");
				}
			}
			return f.toString();
		}
	}

}
