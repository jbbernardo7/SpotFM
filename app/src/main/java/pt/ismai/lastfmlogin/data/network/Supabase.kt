package pt.ismai.lastfmlogin.data.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import pt.ismai.lastfmlogin.utils.Constants

object Supabase {
    private const val SUPABASE_URL = Constants.SUPABASE_URL;

    private const val SUPABASE_KEY = Constants.SUPABASE_KEY;

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
    }
}