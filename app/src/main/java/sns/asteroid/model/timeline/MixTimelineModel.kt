package sns.asteroid.model.timeline

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.Timelines
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.GettingContentsModel.Result

class MixTimelineModel(credential: Credential): AbstractTimelineModel<Status>(credential) {
    override fun getContents(maxId: String?, sinceId: String?): Result<Status> {
        val client = Timelines(credential)

        val getLocal = client.getLocal(maxId, sinceId)
            ?: return Result(isSuccess=false, toastMessage=getString(R.string.failed_loading))
        val getHome = client.getHome(maxId, sinceId)
            ?: return Result(isSuccess=false, toastMessage=getString(R.string.failed_loading))

        if(!getLocal.isSuccessful or !getHome.isSuccessful)
            return Result<Status>(isSuccess=false, toastMessage=getLocal.body!!.string())
                .also {
                    getLocal.close()
                    getHome.close()
                }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val local = json.decodeFromString(ListSerializer(Status.serializer()), getLocal.body!!.string())
        val home = json.decodeFromString(ListSerializer(Status.serializer()), getHome.body!!.string())
        val statuses = integration(local, home)

        if(statuses.isEmpty()) return Result<Status>(isSuccess=true)
            .also {
                getLocal.close()
                getHome.close()
            }

        return Result(
            isSuccess       = true,
            contents        = statuses,
            toastMessage    = null,
            maxId           = statuses.last().id,
            sinceId         = statuses.first().id,
        ).also {
            getLocal.close()
            getHome.close()
        }
    }

    /**
     * LTLとホームを実際に結合する処理
     *
     * TLの終点がLTLとホームで異なるので、終点近い方に合わせて切り落とす
     * （そうしないと古いのを読み込んだときに取得漏れする）
     */
    private fun integration(l: List<Status>, h: List<Status>): List<Status> {
        if(l.isEmpty()) return h
        if(h.isEmpty()) return l
        val local = l.toMutableList()
        val home = h.toMutableList()

        if (local.last().id < home.last().id)
            local.removeAll { it.id < home.last().id }
        else
            home.removeAll { it.id < local.last().id }

        return local.plus(home).toSet().sortedByDescending { it.id }
    }
}