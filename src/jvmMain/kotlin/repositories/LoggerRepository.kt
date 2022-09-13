package repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class LoggerRepository {
    var channel = Channel<String>()
        private set

    init {
        GlobalScope.launch(Dispatchers.IO) {
            for (x in channel) {
                println(x)
            }
        }

    }
}