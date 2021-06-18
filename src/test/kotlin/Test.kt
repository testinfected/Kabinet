import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test

class Test {

    @Test
    fun ready() {
        assertThat(1 + 1, equalTo(2))
    }
}
