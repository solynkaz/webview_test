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
    const val WIKI_JS_BEARER = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcGkiOjEsImdycCI6MSwiaWF0IjoxNjk3MTY0OTA2LCJleHAiOjE3Mjg3MjI1MDYsImF1ZCI6InVybjp3aWtpLmpzIiwiaXNzIjoidXJuOndpa2kuanMifQ.HFAxu393tjFYz8Q6kIFe8aH0SBL7TLLaeSAhBj4XmUD5UjuhshlVQ9VqQIcQ4b8bqgD5W0_iVCgm-CziE6_0sJGUiO4rKeAuA3GGb7ltIJoa63XMYlrs5yU201iGmAJ5zWS87CIt5D4QaUtdF1CDjZXPwdStjftxe1z3YIWUeAT58yp4aSgRmR9l_bATl7sKnEX9B05vlC2WNFHCs1uiNdSULhJdZ0il9Ib95YqI99fS4a4OG_heaqEpYDPgYuLW0HDKUnqPsdCERwqKRtEhQKfuDsd3A-uFM8TmuAlrWfTrrX6yxu4ym5LMDF1lW2461ljUkQalfvRhRzYd0CKmpg"
}

object PREFS_VALUES {
    const val GIT_REPO_URL = "GIT_REPO_URL"
}