/*
 * This file is part of GraphStream <http://graphstream-project.org>.
 * 
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 * 
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 *
 *
 * @since 2012-02-10
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.generator;

import java.net.URISyntaxException;

public class WikipediaGenerator extends URLGenerator {
	public static final String SPECIAL_URLS = "^https://%s[.]wikipedia[.]org/wiki/(Wikipedia|File|Special|Category|Talk|Portal|Help|Template|Template_talk):.*$";

	public static enum Lang {
		EN("en.wikipedia.org", "Main_Page",
				"Wikipedia|File|Special|Category|Talk|Portal|Help|Template|Template_talk"), FR(
				"fr.wikipedia.org", "Wikipédia:Accueil_Principal",
				"Wikipédia|Aide|Spécial|Catégorie|Portail|Discussion|Special")

		;

		final String host;
		final String mainPage;
		final String specialFiles;

		Lang(String host, String mainPage, String special) {
			this.host = host;
			this.mainPage = mainPage;
			this.specialFiles = special;
		}
	}

	protected final Lang lang;

	public WikipediaGenerator(String... articles) {
		this(Lang.EN, articles);
	}

	public WikipediaGenerator(Lang lang, String... articles) {
		this.lang = lang;

		setDirected(true);
		setMode(Mode.PATH);

		addHostFilter(lang.host);

		declineMatchingURL("^https?://" + lang.host + "/wiki/index.php.*");
		declineMatchingURL("^https?://" + lang.host + "/wiki/" + lang.mainPage);
		declineMatchingURL("^https?://" + lang.host + "/wiki/[\\w_]+:.*$");

		acceptOnlyMatchingURL("^https?://" + lang.host + "/wiki/.*$");

		if (articles != null)
			for (int i = 0; i < articles.length; i++)
				addArticle(articles[i]);
	}

	public void addArticle(String name) {
		addURL("https://" + lang.host + "/wiki/" + name);
	}

	@Override
	protected String getNodeLabel(String url) throws URISyntaxException {
		return url.substring(url.indexOf("/wiki/") + 6);
	}
}
