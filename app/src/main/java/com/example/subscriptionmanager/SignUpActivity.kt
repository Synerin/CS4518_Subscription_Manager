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
import com.google.firebase.auth.FirebaseAuth


private const val TAG = "SignUpActivity"

class SignUpActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null

    private lateinit var emailTextView: TextView
    private lateinit var emailEditText: EditText
    private lateinit var passwordTextView: TextView
    private lateinit var passwordEditText: EditText
    private lateinit var loginBackground: ConstraintLayout
    private lateinit var signUpButton: Button
    private lateinit var signInTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        mAuth = FirebaseAuth.getInstance();

        emailTextView = findViewById(R.id.emailTextSignUp)
        emailEditText = findViewById(R.id.editTextEmailAddressSignUp)
        passwordTextView = findViewById(R.id.passwordTextSignUp)
        passwordEditText = findViewById(R.id.editTextPasswordSignUp)
        loginBackground = findViewById(R.id.signUpBackground)
        signUpButton = findViewById(R.id.signUpButton)
        signInTextView = findViewById(R.id.textViewSignUp)

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

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email == "" || password == "") {
                Log.d(TAG, "Email and/or Password Field is blank")
                Toast.makeText(
                    this, "Failed Sign Up: \nEmail and/or Password Field is blank",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            this@SignUpActivity, "Sign Up Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        val ss = SpannableString("Already have an account? Sign In!")
        val clickableSpan: ClickableSpan = object : ClickableSpan() {

            override fun onClick(textView: View) {
                startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = resources.getColor(R.color.colorAccent)
            }
        }
        ss.setSpan(clickableSpan, 25, 33, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val boldSpan = StyleSpan(Typeface.BOLD)
        ss.setSpan(boldSpan, 25, 33, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        signInTextView.text = ss

        signInTextView.movementMethod = LinkMovementMethod.getInstance()
        signInTextView.highlightColor = Color.TRANSPARENT
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