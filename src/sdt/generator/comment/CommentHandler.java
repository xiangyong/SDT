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

@SuppressWarnings("restriction")
public class CommentHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

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

		String[] words = d.fFields.toArray(new String[0]);
		if (words.length == 0)
			return null;

		// 第二次遍历
		d.fAst = ast;
		d.fDic = _.en2cn(words);
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
		public Map<String, Integer> fMax = new HashMap<String, Integer>();

		public SrcVisitor(AST ast) {
			this.fAst = ast;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean visit(FieldDeclaration node) {
			if (node.getJavadoc() != null)
				return true;

			String words = getWords(((VariableDeclarationFragment) node.fragments().get(0)).getName()
					.getFullyQualifiedName());
			if (fAst != null) {
				String cn = getCnWord(words);
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
				getEnWord(words);
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
				int maxLength = fMax.get(node.getName().getFullyQualifiedName());
				// method name
				String cn = getCnWord(words);
				if (cn == null)
					return true;
				List<TagElement> tes = new ArrayList<TagElement>();
				TagElement tag = fAst.newTagElement();
				tag.setTagName("<b>" + node.getName().getFullyQualifiedName() + "</b>\t");
				TextElement txt = fAst.newTextElement();
				tag.fragments().add(txt);
				txt.setText(cn);
				tes.add(tag);

				// parameters
				for (SingleVariableDeclaration var : list) {
					cn = getCnWord(var.getName().getFullyQualifiedName());
					if (cn == null)
						return true;
					tag = fAst.newTagElement();
					tag.setTagName(column("@param ", var.getName().getFullyQualifiedName(), maxLength));
					txt = fAst.newTextElement();
					tag.fragments().add(txt);
					txt.setText(cn);
					tes.add(tag);
				}

				// return
				cn = getCnWord(node.getReturnType2().toString());
				if (cn == null)
					return true;
				tag = fAst.newTagElement();
				tag.setTagName(column("@return", "", maxLength));
				txt = fAst.newTextElement();
				tag.fragments().add(txt);
				txt.setText(cn);
				tes.add(tag);

				// TODO
				Javadoc jd = fAst.newJavadoc();
				jd.tags().addAll(tes);
				node.setJavadoc(jd);
			} else {
				getEnWord(words);
				int max = 0;
				for (SingleVariableDeclaration var : list) {
					String varName = var.getName().getFullyQualifiedName();
					int len = varName.length();
					getEnWord(varName);
					if (len > max)
						max = len;
				}
				fMax.put(node.getName().getFullyQualifiedName(), max);
				getEnWord(node.getReturnType2().toString());
			}
			return true;
		}

		private String getEnWord(String s) {
			s = s.trim();

			int len = s.length();
			String f = s;

			if (len >= 6) {
				String l6 = s.substring(len - 3);
				if (l6.equalsIgnoreCase("Facade")) {
					f = s.substring(0, len - 6);
				}
			}

			if (len >= 3) {
				String l3 = s.substring(len - 3);
				if (l3.equalsIgnoreCase("DAO")) {
					f = s.substring(0, len - 3);
					if (f.startsWith("Ibatis")) {
						f = f.substring("Ibatis".length());
					}
				}
			}

			if (len >= 2) {
				String l2 = s.substring(len - 2);
				if (l2.equalsIgnoreCase("VO") || l2.equalsIgnoreCase("ID") || l2.equalsIgnoreCase("DO")) {
					f = s.substring(0, len - 2);
				}
			}
			if (f.length() == 0)
				return null;
			String enWord = getWords(f);
			System.err.println(enWord);
			fFields.add(enWord);
			return enWord;
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

		private String getCnWord(String s) {
			String cn = fDic.get(getEnWord(s));
			if (cn == null)
				cn = "";
			StringBuffer f = new StringBuffer(cn);
			int len = s.length();
			if (s.length() >= 6) {
				String l6 = s.substring(len - 6);
				if (l6.equalsIgnoreCase("Facade")) {
					f.append(" Facade");
				}
			}
			if (s.length() >= 3) {
				String l3 = s.substring(len - 3);
				if (l3.equalsIgnoreCase("DAO")) {
					if (s.startsWith("Ibatis")) {
						f.append(" Ibatis");
					}
					f.append(" DAO");
				}
			}
			if (len >= 2) {
				String l2 = s.substring(len - 2);
				if (l2.equalsIgnoreCase("VO")) {
					f.append(" VO");
				} else if (l2.equalsIgnoreCase("DO")) {
					f.append(" DO");
				} else if (l2.equalsIgnoreCase("ID")) {
					if (len > 2)
						f.append(" ");
					f.append("ID");
				}
			}

			return f.toString();
		}

		private String column(String s1, String s2, int max) {
			return s1 + rspace(s2, max - s2.length());
		}

		private String rspace(String txt, int length) {
			StringBuffer f = new StringBuffer(txt);
			for (int i = 0; i < length; i++) {
				f.append(" ");
			}
			return f.toString();
		}
	}

}
