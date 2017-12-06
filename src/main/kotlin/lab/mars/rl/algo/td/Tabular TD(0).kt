package lab.mars.rl.algo.td

import lab.mars.rl.algo.td.TemporalDifference.Companion.log
import lab.mars.rl.model.impl.mdp.StateValueFunction
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.util.log.debug

fun TemporalDifference.`Tabular TD(0)`(): StateValueFunction {
  val V = indexedMdp.VFunc { 0.0 }
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var s = started()
    while (s.isNotTerminal) {
      val a = initial_policy(s)
      val (s_next, reward) = a.sample()
      V[s] += α * (reward + γ * V[s_next] - V[s])
      s = s_next
    }
  }
  return V
}