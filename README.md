# ClickAuth 🔑
ClickAuth is an offline authentication plugin for Spigot/Paper that *actually* works.

### Features
- **Effortless.** Ready to use out of the box.
- **Secure.** The server admins cannot see your password.
- No commands needed to log in, input is directly in chat.
- You only have to log in once.
  - Afterward, you'll be logged in **automatically**, unless your IP changes.
- Highly configurable.
- Players cannot execute any commands until they are logged in.

### Security & Privacy
- Passwords are hashed and salted using the [bcrypt](https://en.wikipedia.org/wiki/Bcrypt) algorithm.
- IP addresses are hashed using **SHA256**.

### Commands
- `/reset_password` - Resets your own or the given player's password.
You will then be prompted to enter a new password.
- `/invalidate_session` - Invalidates your own or the given player's session.
This will require them to log in again.

### License
- This project is licensed under the GPLv3 license.
- Refer to LICENSE.md for more information.