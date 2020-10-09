package com.example.subscriptionmanager

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth


private const val TAG = "SignInActivity"

class SignInActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null

    private lateinit var emailTextView: TextView
    private lateinit var emailEditText: EditText
    private lateinit var passwordTextView: TextView
    private lateinit var passwordEditText: EditText
    private lateinit var loginBackground: ConstraintLayout
    private lateinit var signInButton: Button
    private lateinit var signUpTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        mAuth = FirebaseAuth.getInstance();

        emailTextView = findViewById(R.id.emailTextSignIn)
        emailEditText = findViewById(R.id.editTextEmailAddressSignIn)
        passwordTextView = findViewById(R.id.passwordTextSignIn)
        passwordEditText = findViewById(R.id.editTextPasswordSignIn)
        loginBackground = findViewById(R.id.loginBackground)
        signInButton = findViewById(R.id.signInButton)
        signUpTextView = findViewById(R.id.textViewSignUp)

        emailTextView.visibility = View.INVISIBLE

        emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                emailTextView.visibility = View.VISIBLE
            } else if (emailEditText.text.isEmpty()) {
                emailTextView.visibility = View.INVISIBLE
            }
        }

        passwordTextView.visibility = View.INVISIBLE

        passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                passwordTextView.visibility = View.VISIBLE
            } else if (passwordEditText.text.isEmpty()) {
                passwordTextView.visibility = View.INVISIBLE
            }
        }

        loginBackground.setOnClickListener {
            emailEditText.clearFocus()
            passwordEditText.clearFocus()
            closeKeyBoard()
        }

        signInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email == "" || password == "") {
                Log.d(TAG, "Email and/or Password Field is blank")
                Toast.makeText(
                    this, "Failed Login: \nEmail and/or Password Field is blank",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,
                    OnCompleteListener<AuthResult?> { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                this, "Failed Login",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
        }

        val ss = SpannableString("Don't have an account? Sign up!")
        val clickableSpan: ClickableSpan = object : ClickableSpan() {

            override fun onClick(textView: View) {
                startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = resources.getColor(R.color.colorAccent)
            }
        }
        ss.setSpan(clickableSpan, 23, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val boldSpan = StyleSpan(Typeface.BOLD)
        ss.setSpan(boldSpan, 23, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        signUpTextView.text = ss

        signUpTextView.movementMethod = LinkMovementMethod.getInstance()
        signUpTextView.highlightColor = Color.TRANSPARENT
    }

    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onBackPressed() {}
}