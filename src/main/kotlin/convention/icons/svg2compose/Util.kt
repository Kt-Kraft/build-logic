package convention.icons.svg2compose

import java.util.Stack

public fun <T> Stack<T>.peekOrNull(): T? = runCatching { peek() }.getOrNull()
