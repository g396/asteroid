package sns.asteroid.model.user

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.Accounts
import sns.asteroid.api.entities.Relationship
import sns.asteroid.db.entities.Credential

class RelationshipModel(val credential: Credential, val ids: List<String>) {
    data class Result(
        val isSuccess: Boolean,
        val relationship: List<Relationship>?,
        val toastMessage: String?,
    )

    constructor(credential: Credential, id: String) : this(credential, listOf(id))

    fun getRelationship(): Result {
        val client = Accounts(credential)
        val response = client.getRelationships(ids)
            ?: return Result(isSuccess = false, relationship = null, toastMessage = getString(R.string.failed))

        if(!response.isSuccessful) {
            return Result(isSuccess = false, relationship = null, toastMessage = response.body!!.string())
        }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val relationships = json.decodeFromString(ListSerializer(Relationship.serializer()), response.body!!.string())

        return if(relationships.isEmpty()) Result(isSuccess = true, relationship = null, toastMessage = null)
        else Result(isSuccess = true, relationship = relationships, toastMessage = null)
    }

    fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }
}