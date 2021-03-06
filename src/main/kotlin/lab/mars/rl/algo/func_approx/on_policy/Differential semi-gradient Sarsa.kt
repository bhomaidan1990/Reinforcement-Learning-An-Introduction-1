package lab.mars.rl.algo.func_approx.on_policy

import lab.mars.rl.model.ApproximateFunction
import lab.mars.rl.model.MDP
import lab.mars.rl.model.Policy
import lab.mars.rl.util.matrix.times

fun <E> MDP.`Differential semi-gradient Sarsa`(
    q: ApproximateFunction<E>, π: Policy,
    α: Double, β: Double, maxStep: Int) {
  var average_reward = 0.0
  var s = started()
  var a = π(s)
  var step = 0
  while (true) {
    val (s_next, reward) = a.sample()
    val a_next = π(s_next)
    val δ = reward - average_reward + q(s_next, a_next) - q(s, a)
    average_reward += β * δ
    q.w += α * δ * q.`∇`(s, a)
    s = s_next
    a = a_next
    step++
    if (step >= maxStep) break
  }
}