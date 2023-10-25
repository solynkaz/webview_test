package com.example.webview

object AppConsts {
    const val KB_URL = "https://kb.sibdigital.net/"
    const val KB_FOLDER = "KnowledgeBase"
    const val GIT_FOLDER = "Git"
    const val GET_REPO_URL_QUERY = """
        query {
          storage{
            targets {
              title
              config {
                key
                value
              }
            }
          }
        }
    """
    const val EMPTY_MD_STRING = "Нет данных для отображения"

}
object PREFS_VALUES {
    const val PREFS = "PREFS"
    const val PAGE_TITLE = "PAGE_TITLE"
    const val GIT_REPO_URL = "GIT_REPO_URL"
    const val IS_REPO_CLONED = "IS_REPO_CLONED"
    const val GIT_LOGIN = "PREF_LOGIN"
    const val GIT_PASS = "PREF_PASS"
    const val WIKI_JS_BEARER = "PREF_WIKI_JS_BEARER"
}

object PAGES {
    const val MAIN_MENU = "Главная"
    const val SETTINGS = "Настройки"
}

object IGNORE_FILES {

}