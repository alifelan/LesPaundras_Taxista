package ViewModels

import ApiUtility.Taxi
import ApiUtility.User
import android.arch.lifecycle.ViewModel

/**
 * Used to share user data in NavbarActivity
 */
class UserViewModel: ViewModel() {
    var taxi: Taxi? = null
}