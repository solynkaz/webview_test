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
}
object PREFS_VALUES {
    const val GIT_REPO_URL = "GIT_REPO_URL"
    const val IS_REPO_CLONED = "IS_REPO_CLONED"
    const val GITLAB_LOGIN = "PREF_LOGIN"
    const val GITLAB_PASS = "PREF_PASS"
    const val WIKI_JS_BEARER = "PREF_WIKI_JS_BEARER"
}